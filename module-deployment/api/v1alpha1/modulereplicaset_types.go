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

// ModuleReplicaSetSpec defines the desired state of ModuleReplicaSet
type ModuleReplicaSetSpec struct {
	Replicas int `json:"replicas,omitempty"`

	Selector *metav1.LabelSelector `json:"selector,omitempty"`

	Template ModuleTemplate `json:"template,omitempty"`
}

// ModuleReplicaSetStatus defines the observed state of ModuleReplicaSet
type ModuleReplicaSetStatus struct {
	// Replicas is the number of actual replicas.
	Replicas int32 `json:"replicas"`

	// The number of ready replicas for this replica set.
	ReadyReplicas int32 `json:"readyReplicas"`

	// The number of available replicas (ready for at least minReadySeconds) for this replica set.
	AvailableReplicas int32 `json:"availableReplicas"`

	// ObservedGeneration is the most recent generation observed by the controller.
	ObservedGeneration int64 `json:"observedGeneration"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// ModuleReplicaSet is the Schema for the modulereplicasets API
type ModuleReplicaSet struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ModuleReplicaSetSpec   `json:"spec,omitempty"`
	Status ModuleReplicaSetStatus `json:"status,omitempty"`
}

//+kubebuilder:object:root=true

// ModuleReplicaSetList contains a list of ModuleReplicaSet
type ModuleReplicaSetList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []ModuleReplicaSet `json:"items"`
}

func init() {
	SchemeBuilder.Register(&ModuleReplicaSet{}, &ModuleReplicaSetList{})
}
