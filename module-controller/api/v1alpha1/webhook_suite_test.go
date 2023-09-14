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
	"crypto/tls"
	"fmt"
	v1 "k8s.io/api/apps/v1"
	corev1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes/scheme"
	"net"
	"path/filepath"
	"testing"
	"time"

	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"

	admissionv1 "k8s.io/api/admission/v1"

	"k8s.io/client-go/rest"
	ctrl "sigs.k8s.io/controller-runtime"
	"sigs.k8s.io/controller-runtime/pkg/client"
	"sigs.k8s.io/controller-runtime/pkg/envtest"
	logf "sigs.k8s.io/controller-runtime/pkg/log"
	"sigs.k8s.io/controller-runtime/pkg/log/zap"
)

// These tests use Ginkgo (BDD-style Go testing framework). Refer to
// http://onsi.github.io/ginkgo/ to learn more about Ginkgo.

var cfg *rest.Config
var k8sClient client.Client
var testEnv *envtest.Environment
var ctx context.Context
var cancel context.CancelFunc

func TestAPIs(t *testing.T) {
	RegisterFailHandler(Fail)

	RunSpecs(t, "Webhook Suite")
}

var _ = BeforeSuite(func() {
	logf.SetLogger(zap.New(zap.WriteTo(GinkgoWriter), zap.UseDevMode(true)))

	ctx, cancel = context.WithCancel(context.TODO())

	By("bootstrapping test environment")
	testEnv = &envtest.Environment{
		CRDDirectoryPaths:     []string{filepath.Join("..", "..", "config", "crd", "bases")},
		ErrorIfCRDPathMissing: false,
		WebhookInstallOptions: envtest.WebhookInstallOptions{
			Paths: []string{filepath.Join("..", "..", "config", "webhook")},
		},
	}

	var err error
	// cfg is defined in this file globally.
	cfg, err = testEnv.Start()
	Expect(err).NotTo(HaveOccurred())
	Expect(cfg).NotTo(BeNil())

	err = scheme.AddToScheme(scheme.Scheme)
	Expect(err).NotTo(HaveOccurred())

	err = AddToScheme(scheme.Scheme)
	Expect(err).NotTo(HaveOccurred())

	err = admissionv1.AddToScheme(scheme.Scheme)
	Expect(err).NotTo(HaveOccurred())

	//+kubebuilder:scaffold:scheme

	k8sClient, err = client.New(cfg, client.Options{Scheme: scheme.Scheme})
	Expect(err).NotTo(HaveOccurred())
	Expect(k8sClient).NotTo(BeNil())

	// start webhook server using Manager
	webhookInstallOptions := &testEnv.WebhookInstallOptions
	mgr, err := ctrl.NewManager(cfg, ctrl.Options{
		Scheme:             scheme.Scheme,
		Host:               webhookInstallOptions.LocalServingHost,
		Port:               webhookInstallOptions.LocalServingPort,
		CertDir:            webhookInstallOptions.LocalServingCertDir,
		LeaderElection:     false,
		MetricsBindAddress: "0",
	})
	Expect(err).NotTo(HaveOccurred())

	err = (&ModuleDeployment{}).SetupWebhookWithManager(mgr)
	Expect(err).NotTo(HaveOccurred())

	//+kubebuilder:scaffold:webhook

	go func() {
		defer GinkgoRecover()
		err = mgr.Start(ctx)
		Expect(err).NotTo(HaveOccurred())
	}()

	// wait for the webhook server to get ready
	dialer := &net.Dialer{Timeout: time.Second}
	addrPort := fmt.Sprintf("%s:%d", webhookInstallOptions.LocalServingHost, webhookInstallOptions.LocalServingPort)
	Eventually(func() error {
		conn, err := tls.DialWithDialer(dialer, "tcp", addrPort, &tls.Config{InsecureSkipVerify: true})
		if err != nil {
			return err
		}
		conn.Close()
		return nil
	}).Should(Succeed())

})

var _ = AfterSuite(func() {
	cancel()
	By("tearing down the test environment")
	err := testEnv.Stop()
	Expect(err).NotTo(HaveOccurred())
})

func TestWebhookDefault(t *testing.T) {
	r := ModuleDeployment{}
	t.Run("", func(t *testing.T) {
		r.Default()
		r.ValidateDelete()
	})
}

var _ = Describe("ModuleDeployment Webhook", func() {

	namespace := "default"
	moduleDeploymentName := "module-deployment-test-demo"
	moduleDeployment := prepareModuleDeployment(namespace, moduleDeploymentName)

	Context("create module deployment failed", func() {
		It("create module deployment failed", func() {
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).ShouldNot(Succeed())

			warnings, err := moduleDeployment.ValidateCreate()
			Expect(err).ShouldNot(BeNil())
			Expect(warnings).ShouldNot(BeNil())
		})
	})

	Context("update module deployment failed", func() {
		It("update module deployment failed", func() {
			Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).ShouldNot(Succeed())
			warnings, err := moduleDeployment.ValidateUpdate(&moduleDeployment)
			Expect(err).ShouldNot(BeNil())
			Expect(warnings).ShouldNot(BeNil())
		})
	})

	Context("create module deployment success", func() {
		It("create module deployment success", func() {

			deployment := prepareDeployment()
			Expect(k8sClient.Create(context.TODO(), &deployment)).Should(Succeed())
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).Should(Succeed())

			createWarnings, createErr := moduleDeployment.ValidateCreate()
			Expect(createErr).Should(BeNil())
			Expect(createWarnings).Should(BeNil())

			Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).Should(Succeed())
			updateWarnings, updateErr := moduleDeployment.ValidateUpdate(&moduleDeployment)
			Expect(updateErr).Should(BeNil())
			Expect(updateWarnings).Should(BeNil())

			Expect(k8sClient.Delete(context.TODO(), &moduleDeployment)).Should(Succeed())
			Expect(k8sClient.Delete(context.TODO(), &deployment)).Should(Succeed())
		})
	})

	Context("create module deployment replicas failed", func() {
		It("create module deployment replicas failed", func() {

			deployment := prepareDeployment()
			moduleDeployment.Spec.Replicas = 2
			Expect(k8sClient.Create(context.TODO(), &deployment)).Should(Succeed())
			Expect(k8sClient.Create(context.TODO(), &moduleDeployment)).ShouldNot(Succeed())

			createWarnings, createErr := moduleDeployment.ValidateCreate()
			Expect(createErr).ShouldNot(BeNil())
			Expect(createWarnings).ShouldNot(BeNil())

			Expect(k8sClient.Update(context.TODO(), &moduleDeployment)).ShouldNot(Succeed())
			updateWarnings, updateErr := moduleDeployment.ValidateUpdate(&moduleDeployment)
			Expect(updateErr).ShouldNot(BeNil())
			Expect(updateWarnings).ShouldNot(BeNil())
		})
	})
})

func prepareDeployment() v1.Deployment {
	var deployment v1.Deployment
	replicas := int32(1)
	deployment = v1.Deployment{
		ObjectMeta: metav1.ObjectMeta{
			Name:      "dynamic-stock-deployment",
			Namespace: "default",
		},
		Spec: v1.DeploymentSpec{
			Replicas: &replicas,
			Selector: &metav1.LabelSelector{
				MatchLabels: map[string]string{
					"app": "dynamic-stock",
				},
			},
			Template: corev1.PodTemplateSpec{
				ObjectMeta: metav1.ObjectMeta{
					Labels: map[string]string{
						"app": "dynamic-stock",
					},
				},
				Spec: corev1.PodSpec{
					Containers: []corev1.Container{
						{
							Name:  "dynamic-stock-deployment",
							Image: "serverless-registry.cn-shanghai.cr.aliyuncs.com/opensource/test/dynamic-stock-mng:v0.8",
							Ports: []corev1.ContainerPort{
								{
									ContainerPort: 8080,
								},
								{
									ContainerPort: 1238,
								},
							},
						},
					},
				},
			},
		},
	}
	return deployment
}

func prepareModuleDeployment(namespace, moduleDeploymentName string) ModuleDeployment {
	baseDeploymentName := "dynamic-stock-deployment"

	moduleDeployment := ModuleDeployment{
		Spec: ModuleDeploymentSpec{
			BaseDeploymentName: baseDeploymentName,
			DeployType:         ModuleDeploymentDeployTypeSymmetric,
			Replicas:           1,
			Template: ModuleTemplateSpec{
				Spec: ModuleSpec{
					Module: ModuleInfo{
						Name:    "dynamic-provider",
						Version: "1.0.0",
						Url:     "http://serverless-opensource.oss-cn-shanghai.aliyuncs.com/module-packages/stable/dynamic-provider-1.0.0-ark-biz.jar",
					},
				},
			},
		},
		ObjectMeta: metav1.ObjectMeta{
			Name:      moduleDeploymentName,
			Namespace: namespace,
			Labels: map[string]string{
				"app": "dynamic-stock",
			},
			Annotations: map[string]string{},
		},
	}
	return moduleDeployment
}
