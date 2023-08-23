package validation

import (
	moduledeploymentv1alpha1 "github.com/sofastack/sofa-serverless/api/v1alpha1"
	"github.com/stretchr/testify/assert"
	v1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime/schema"
	"testing"
)

func resource(resource string) schema.GroupResource {
	return schema.GroupResource{Group: "", Resource: resource}
}

func TestModuleDeploymentCheckNameEmpty(t *testing.T) {
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	assert.True(t, ModuleDeploymentCheck(moduleDeployment))
	assert.True(t, len(moduleDeployment.Status.Conditions) == 1)
	condition := moduleDeployment.Status.Conditions[0]
	assert.True(t, condition.Message == DeploymentNameEmptyErrMessage)
	assert.True(t, condition.Reason == "deploymentName can not be null")
	assert.True(t, condition.Status == corev1.ConditionFalse)
	assert.True(t, condition.Type == moduledeploymentv1alpha1.DeploymentReplicaFailure)
}

func TestModuleDeploymentCheckDeployType(t *testing.T) {
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	moduleDeployment.Spec.DeploymentName = "test"
	moduleDeployment.Spec.DeployType = "none"
	assert.True(t, ModuleDeploymentCheck(moduleDeployment))
	assert.True(t, len(moduleDeployment.Status.Conditions) == 1)
	condition := moduleDeployment.Status.Conditions[0]
	assert.True(t, condition.Message == DeployTypeErrMessage)
	assert.True(t, condition.Reason == "deployType not support")
	assert.True(t, condition.Status == corev1.ConditionFalse)
	assert.True(t, condition.Type == moduledeploymentv1alpha1.DeploymentReplicaFailure)
}

func TestModuleDeploymentCheckModuleInfo(t *testing.T) {
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	moduleDeployment.Spec.DeploymentName = "test"
	moduleDeployment.Spec.DeployType = SymmetricDeployType
	assert.True(t, ModuleDeploymentCheck(moduleDeployment))
	assert.True(t, len(moduleDeployment.Status.Conditions) == 1)
	condition := moduleDeployment.Status.Conditions[0]
	assert.True(t, condition.Message == ModuleInfoErrMessage)
	assert.True(t, condition.Reason == "module name or version or url can not empty")
	assert.True(t, condition.Status == corev1.ConditionFalse)
	assert.True(t, condition.Type == moduledeploymentv1alpha1.DeploymentReplicaFailure)
}

func TestDeploymentCheck(t *testing.T) {
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	deployment := &v1.Deployment{}
	check := DeploymentCheck(errors.NewNotFound(resource("test"), "NotFound"), moduleDeployment, deployment)
	assert.True(t, check)
	assert.True(t, len(moduleDeployment.Status.Conditions) == 1)
	condition := moduleDeployment.Status.Conditions[0]
	assert.True(t, condition.Message == DeploymentNotFoundErrMessage)
	assert.True(t, condition.Reason == "deployment not found")
	assert.True(t, condition.Status == corev1.ConditionFalse)
	assert.True(t, condition.Type == moduledeploymentv1alpha1.DeploymentReplicaFailure)
}

func TestReplicasCheck(t *testing.T) {
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	deployment := &v1.Deployment{}
	replicas := int32(1)
	deployment.Spec.Replicas = &replicas
	check := ReplicasCheck(moduleDeployment, deployment, 2)
	assert.True(t, check)
	assert.True(t, len(moduleDeployment.Status.Conditions) == 1)
	condition := moduleDeployment.Status.Conditions[0]
	assert.True(t, condition.Message == ReplicasCheckErrMessage)
	assert.True(t, condition.Reason == "replicas more than deployment replicas")
	assert.True(t, condition.Status == corev1.ConditionFalse)
	assert.True(t, condition.Type == moduledeploymentv1alpha1.DeploymentReplicaFailure)
}
