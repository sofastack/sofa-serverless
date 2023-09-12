/*
Copyright 2023.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package v1alpha1

import (
	"context"
	v1 "k8s.io/api/apps/v1"
	"k8s.io/apimachinery/pkg/api/errors"
	"k8s.io/apimachinery/pkg/runtime"
	"k8s.io/apimachinery/pkg/types"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/webhook"
	"sigs.k8s.io/controller-runtime/pkg/webhook/admission"
)

var c client.Client

// log is for logging in this package.
var moduledeploymentlog = logf.Log.WithName("moduledeployment-resource")

func (r *ModuleDeployment) SetupWebhookWithManager(mgr ctrl.Manager) error {
	c = mgr.GetClient()
	return ctrl.NewWebhookManagedBy(mgr).
		For(r).
		Complete()
}

// TODO(user): EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!

//+kubebuilder:webhook:path=/mutate-serverless-alipay-com-v1alpha1-moduledeployment,mutating=true,failurePolicy=fail,sideEffects=None,groups=serverless.alipay.com,resources=moduledeployments,verbs=create;update,versions=v1alpha1,name=mmoduledeployment.kb.io,admissionReviewVersions=v1

var _ webhook.Defaulter = &ModuleDeployment{}

// Default implements webhook.Defaulter so a webhook will be registered for the type
func (r *ModuleDeployment) Default() {
	moduledeploymentlog.Info("default", "name", r.Name)

	// TODO(user): fill in your defaulting logic.
}

// TODO(user): change verbs to "verbs=create;update;delete" if you want to enable deletion validation.
//+kubebuilder:webhook:path=/validate-serverless-alipay-com-v1alpha1-moduledeployment,mutating=false,failurePolicy=fail,sideEffects=None,groups=serverless.alipay.com,resources=moduledeployments,verbs=create;update,versions=v1alpha1,name=vmoduledeployment.kb.io,admissionReviewVersions=v1

var _ webhook.Validator = &ModuleDeployment{}

// ValidateCreate implements webhook.Validator so a webhook will be registered for the type
func (r *ModuleDeployment) ValidateCreate() (admission.Warnings, error) {
	moduledeploymentlog.Info("validate create", "name", r.Name)

	warning, err := r.replicasCheck()
	// TODO(user): fill in your validation logic upon object creation.
	return warning, err
}

// ValidateUpdate implements webhook.Validator so a webhook will be registered for the type
func (r *ModuleDeployment) ValidateUpdate(old runtime.Object) (admission.Warnings, error) {
	moduledeploymentlog.Info("validate update", "name", r.Name)

	warning, err := r.replicasCheck()
	// TODO(user): fill in your validation logic upon object update.
	return warning, err
}

// ValidateDelete implements webhook.Validator so a webhook will be registered for the type
func (r *ModuleDeployment) ValidateDelete() (admission.Warnings, error) {
	moduledeploymentlog.Info("validate delete", "name", r.Name)

	// TODO(user): fill in your validation logic upon object deletion.
	return nil, nil
}

func (r *ModuleDeployment) replicasCheck() (admission.Warnings, error) {

	deployment := &v1.Deployment{}
	err := c.Get(context.TODO(), types.NamespacedName{Namespace: r.Namespace, Name: r.Spec.BaseDeploymentName}, deployment)
	if err != nil {
		moduledeploymentlog.Error(err, "Failed to get deployment", "deploymentName", deployment.Name, "namespace", r.Namespace)
		return admission.Warnings{"ReplicasCheck get Deployment Warning"}, err
	}
	if deployment.Spec.Replicas == nil || *deployment.Spec.Replicas < r.Spec.Replicas {
		return admission.Warnings{"ModuleDeployment Replicas can't more then Deployment.apps Replicas"}, errors.NewBadRequest("ModuleDeployment Replicas can't more then Deployment.apps Replicas")
	}
	return nil, nil
}
