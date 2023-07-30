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

type ModuleState int

const (
	_           ModuleState = iota
	UNRESOLVED              // 未注册，此时Module未被运行时解析
	RESOLVED                // Module 解析完成，且已注册，此时 Biz 包还没有安装或者安装中
	ACTIVATED               // Module 启动完成，且处于激活状态，可以对外提供服务
	BROKEN                  // Module 启动失败后状态
	DEACTIVATED             // Module 启动完成，但处于未激活状态
)

// ModuleSpec defines the desired state of Module
type ModuleSpec struct {
	Url        string `json:"url,omitempty"`
	Version    string `json:"version,omitempty"`
	BaseModule string `json:"baseModule,omitempty"`
	PodName    string `json:"podName,omitempty"`
}

// ModuleStatus defines the observed state of Module
type ModuleStatus struct {
	State ModuleState `json:"state,omitempty"`
}

//+kubebuilder:object:root=true
//+kubebuilder:subresource:status

// Module is the Schema for the modules API
type Module struct {
	metav1.TypeMeta   `json:",inline"`
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec   ModuleSpec   `json:"spec,omitempty"`
	Status ModuleStatus `json:"status,omitempty"`
}

type ModuleTemplate struct {
	metav1.ObjectMeta `json:"metadata,omitempty"`

	Spec ModuleSpec `json:"spec,omitempty"`
}

//+kubebuilder:object:root=true

// ModuleList contains a list of Module
type ModuleList struct {
	metav1.TypeMeta `json:",inline"`
	metav1.ListMeta `json:"metadata,omitempty"`
	Items           []Module `json:"items"`
}

func init() {
	SchemeBuilder.Register(&Module{}, &ModuleList{})
}
