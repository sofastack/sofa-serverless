package validation

import (
	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	v1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"sigs.k8s.io/controller-runtime/pkg/log"
)

const (
	SymmetricDeployType           = "symmetric"
	AsymmetricDeployType          = "asymmetric"
	DeployTypeErrMessage          = "deployType only support symmetric or asymmetric"
	ModuleInfoErrMessage          = "module name version url can not empty"
	ReplicasCheckErrMessage       = "Failed to create Replicas, find moduleDeployment Replicas more than Deployment Replicas"
	DeploymentNotFoundErrMessage  = "deployment not found"
	DeploymentNameEmptyErrMessage = "can not create or update, deploymentName can not be ''"
)

func ModuleDeploymentCheck(moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment) bool {
	// check deploymentName is not empty
	deploymentName := moduleDeployment.Spec.DeploymentName
	if deploymentName == "" {
		done := checkCondition(DeploymentNameEmptyErrMessage, moduleDeployment)
		if done {
			return true
		}
		failedCondition := buildCondition("deploymentName can not be null", DeploymentNameEmptyErrMessage)
		moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
		return true
	}

	// deployType check only support symmetric or asymmetric
	deployType := moduleDeployment.Spec.DeployType
	if deployType == "" || !deployTypeSupportCheck(deployType) {
		done := checkCondition(DeployTypeErrMessage, moduleDeployment)
		if done {
			return true
		}
		failedCondition := buildCondition("deployType not support", DeployTypeErrMessage)
		moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
		return true
	}

	// module info check name or version or url can't be ""
	module := moduleDeployment.Spec.Template.Spec.Module
	if module.Name == "" || module.Version == "" || module.Url == "" {
		done := checkCondition(ModuleInfoErrMessage, moduleDeployment)
		if done {
			return true
		}
		failedCondition := buildCondition("module name or version or url can not empty", ModuleInfoErrMessage)
		moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
		return true
	}
	return false
}

func DeploymentCheck(err error, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment, deployment *v1.Deployment) bool {
	if !errors.IsNotFound(err) {
		return false
	}
	done := checkCondition(DeploymentNotFoundErrMessage, moduleDeployment)
	if done {
		return true
	}
	log.Log.Info("Failed to get deployment", "deploymentName", deployment.Name)
	failedCondition := buildCondition("deployment not found", DeploymentNotFoundErrMessage)
	moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
	return true
}

func ReplicasCheck(moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment, deployment *v1.Deployment, moduleReplicas int32) bool {
	deploymentReplicas := *deployment.Spec.Replicas
	if moduleReplicas <= deploymentReplicas {
		return false
	}
	log.Log.Info("Failed to check replicas deployment", "moduleDeploymentName", moduleDeployment.Name)
	done := checkCondition(ReplicasCheckErrMessage, moduleDeployment)
	if done {
		return true
	}
	failedCondition := buildCondition("replicas more than deployment replicas", ReplicasCheckErrMessage)
	moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
	return true
}

func deployTypeSupportCheck(deployType string) bool {
	return deployType == SymmetricDeployType || deployType == AsymmetricDeployType
}

func checkCondition(message string, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment) bool {
	if len(moduleDeployment.Status.Conditions) <= 0 {
		return false
	}
	for _, condition := range moduleDeployment.Status.Conditions {
		if condition.Message == message {
			return true
		}
	}
	return false
}

func buildCondition(reason string, message string) moduledeploymentv1alpha1.ModuleDeploymentCondition {
	var failedCondition moduledeploymentv1alpha1.ModuleDeploymentCondition
	failedCondition.Reason = reason
	failedCondition.Message = message
	failedCondition.Status = corev1.ConditionFalse
	failedCondition.LastTransitionTime = metav1.Now()
	failedCondition.Type = moduledeploymentv1alpha1.DeploymentReplicaFailure
	return failedCondition
}
