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
	ReplicasCheckErrMessage      = "Failed to create Replicas, find moduleDeployment Replicas more than Deployment Replicas"
	DeploymentNotFoundErrMessage = "deployment not found"
)

func DeploymentCheck(err error, moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment, deployment *v1.Deployment) bool {
	if !errors.IsNotFound(err) {
		return true
	}
	done := checkCondition(DeploymentNotFoundErrMessage, moduleDeployment)
	if done {
		return false
	}
	log.Log.Info("Failed to get deployment", "deploymentName", deployment.Name)
	failedCondition := buildCondition("deployment not found", DeploymentNotFoundErrMessage)
	moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
	return false
}

func ReplicasCheck(moduleDeployment *moduledeploymentv1alpha1.ModuleDeployment, deployment *v1.Deployment, moduleReplicas int32) bool {
	deploymentReplicas := *deployment.Spec.Replicas
	if moduleReplicas <= deploymentReplicas {
		return true
	}
	log.Log.Info("Failed to check replicas deployment", "moduleDeploymentName", moduleDeployment.Name)
	done := checkCondition(ReplicasCheckErrMessage, moduleDeployment)
	if done {
		return false
	}
	failedCondition := buildCondition("replicas more than deployment replicas", ReplicasCheckErrMessage)
	moduleDeployment.Status.Conditions = append(moduleDeployment.Status.Conditions, failedCondition)
	return false
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
