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
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

// EDIT THIS FILE!  THIS IS SCAFFOLDING FOR YOU TO OWN!
// NOTE: json tags are required.  Any new fields you add must have json tags for the fields to be serialized.

// ModuleDeploymentSpec defines the desired state of ModuleDeployment
type ModuleDeploymentSpec struct {
	// Number of desired pods.
	Replicas int32 `json:"replicas,omitempty"`

	// Label selector for pods. Existing ReplicaSets whose pods are
	// selected by this will be the ones affected by this ModuleDeployment.
	Selector *metav1.LabelSelector `json:"selector,omitempty"`

	// Template describes the pods that will be created.
	// The only allowed template.spec.restartPolicy value is "Always".
	Template ModuleTemplate `json:"template,omitempty"`

	// The ModuleDeployment strategy to use to replace existing pods with new ones.
	Strategy string `json:"strategy,omitempty"`

	// Minimum number of seconds for which a newly created pod should be ready
	// without any of its container crashing, for it to be considered available.
	// Defaults to 0 (pod will be considered available as soon as it is ready)
	MinReadySeconds int32 `json:"minReadySeconds"`

	// The number of old ReplicaSets to retain to allow rollback.
	// This is a pointer to distinguish between explicit zero and not specified.
	// This is set to the max value of int32 (i.e. 2147483647) by default, which means
	// "retaining all old ReplicaSets".
	RevisionHistoryLimit *int32 `json:"revisionHistoryLimit"`

	// Indicates that the ModuleDeployment is paused and will not be processed by the
	// controller.
	Paused bool `json:"paused"`
}

// ModuleDeploymentStatus defines the observed state of ModuleDeployment
type ModuleDeploymentStatus struct {
	// The generation observed by the ModuleDeployment controller.
	ObservedGeneration int64 `json:"observedGeneration"`

	// Total number of non-terminated pods targeted by this ModuleDeployment (their labels match the selector).
	Replicas int32 `json:"replicas"`

	// Total number of non-terminated pods targeted by this ModuleDeployment that have the desired template spec.
	UpdatedReplicas int32 `json:"updatedReplicas"`

	// Total number of ready pods targeted by this ModuleDeployment.
	ReadyReplicas int32 `json:"readyReplicas"`

	// Total number of available pods (ready for at least minReadySeconds) targeted by this ModuleDeployment.
	AvailableReplicas int32 `json:"availableReplicas"`

	UnavailableReplicas int32 `json:"unavailableReplicas"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// ModuleDeployment is the Schema for the moduledeployments API
type ModuleDeployment struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ModuleDeploymentSpec   `json:"spec,omitempty"`
	Status ModuleDeploymentStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// ModuleDeploymentList contains a list of ModuleDeployment
type ModuleDeploymentList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ModuleDeployment `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ModuleDeployment{}, &ModuleDeploymentList{})
}
