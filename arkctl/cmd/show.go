/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cmd

import (
	"fmt"
	"github.com/spf13/cobra"
)

var moduleName string
var baseName string

// showCmd represents the show command
var showCmd = &cobra.Command{
	Use:   "show",
	Short: "show serverless app status",
	Long:  ``,
	Run:   show,
}

func init() {
	rootCmd.AddCommand(showCmd)
	showCmd.Flags().String("r", "", "")
	showCmd.Flags().String("mc", "", "Get Metadata in MetadataCenter")
	showCmd.Flags().String("h", "h", "")
	showCmd.Flags().StringVarP(&moduleName, "module", "m", "", "Shows the modules information and status")
	showCmd.Flags().StringVarP(&baseName, "base", "b", "", "Shows the modules on the base and their status")
}

func show(cmd *cobra.Command, _ []string) {

	var moduleStatus map[string]string
	var bases map[string][]string

	fmt.Printf("======================\n")
	// fmt.Printf("获取模块状态信息")

	moduleStatus = getModuleStatus()
	bases = getBases()

	// Check for the presence of a module or base
	_, existsBase := bases[baseName]
	_, existsModule := moduleStatus[moduleName]

	if !existsBase || !existsModule {
		if !existsBase && baseName != "" {
			fmt.Printf("[ERROR]：Base %s does not exist.\n", baseName)
		}
		if !existsModule && moduleName != "" {
			fmt.Printf("[ERROR]：Module %s does not exist.\n", moduleName)
		}
	}

	// Displays information about the module or base
	if existsBase && existsModule {
		// Look for the correct base where the module is located
		if contains(bases[baseName], moduleName) {
			fmt.Printf("Module: %s (Status: %s)\n", moduleName, moduleStatus[moduleName])
		} else {
			// Locate the correct base where the module is located
			correctBase := ""
			for base, modules := range bases {
				if contains(modules, moduleName) {
					correctBase = base
					break
				}
			}
			fmt.Printf("Module %s is not on base %s. It is located on base %s (Status: %s)\n", moduleName, baseName, correctBase, moduleStatus[moduleName])
		}
	} else if existsModule {
		// Displays the base on which the module is located and its health
		for base, modules := range bases {
			if contains(modules, moduleName) {
				fmt.Printf("Module: %s is on Base: %s (Status: %s)\n", moduleName, base, moduleStatus[moduleName])
				continue
			}
		}
	} else if existsBase {
		// Displays the number of all modules on the base and the health of the modules
		fmt.Printf("Base: %s contains %d modules\n", baseName, len(bases[baseName]))
		for _, module := range bases[baseName] {
			fmt.Printf("- Module: %s (Status: %s)\n", module, moduleStatus[module])
		}
	} else {
		fmt.Printf("Please give the correct base name or module name.\n")
	}
}

// Check if the slice contains a string
func contains(slice []string, item string) bool {
	for _, s := range slice {
		if s == item {
			return true
		}
	}
	return false
}

// get modules status information
func getModuleStatus() map[string]string {
	var modulestatus = make(map[string]string)
	return modulestatus
}

// get base information
func getBases() map[string][]string {
	var base = make(map[string][]string)
	return base
}
