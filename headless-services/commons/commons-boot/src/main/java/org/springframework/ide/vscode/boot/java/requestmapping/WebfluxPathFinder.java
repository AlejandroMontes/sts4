/*******************************************************************************
 * Copyright (c) 2018 Pivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.vscode.boot.java.requestmapping;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * @author Martin Lippert
 */
public class WebfluxPathFinder extends ASTVisitor {
	
	private String path;
	private ASTNode root;
	
	public WebfluxPathFinder(ASTNode root) {
		this.root = root;
	}
	
	public String getPath() {
		return path;
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		boolean visitChildren = true;

		if (node != this.root) {
			IMethodBinding methodBinding = node.resolveMethodBinding();
			
			if (WebfluxRouterSymbolProvider.REQUEST_PREDICATES_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
				String name = methodBinding.getName();
				if (name != null && WebfluxRouterSymbolProvider.REQUEST_PREDICATE_PATH_METHODS.contains(name)) {
					path = WebfluxRouterSymbolProvider.extractPath(node);
				}
			}
			
			if (WebfluxRouterSymbolProvider.ROUTER_FUNCTIONS_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
				String name = methodBinding.getName();
				if ("route".equals(name)) {
					visitChildren = false;
				}
			}
			else if (WebfluxRouterSymbolProvider.ROUTER_FUNCTION_TYPE.equals(methodBinding.getDeclaringClass().getBinaryName())) {
				String name = methodBinding.getName();
				if ("andRoute".equals(name)) {
					visitChildren = false;
				}
			}
		}
		return visitChildren;
	}

}
