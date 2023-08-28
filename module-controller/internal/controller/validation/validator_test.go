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

func TestDeploymentCheck(t *testing.T) {
	moduleDeployment := &moduledeploymentv1alpha1.ModuleDeployment{}
	deployment := &v1.Deployment{}
	check := DeploymentCheck(errors.NewNotFound(resource("test"), "NotFound"), moduleDeployment, deployment)
	assert.False(t, check)
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
	assert.False(t, check)
	assert.True(t, len(moduleDeployment.Status.Conditions) == 1)
	condition := moduleDeployment.Status.Conditions[0]
	assert.True(t, condition.Message == ReplicasCheckErrMessage)
	assert.True(t, condition.Reason == "replicas more than deployment replicas")
	assert.True(t, condition.Status == corev1.ConditionFalse)
	assert.True(t, condition.Type == moduledeploymentv1alpha1.DeploymentReplicaFailure)
}
