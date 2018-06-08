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
package org.springframework.ide.vscode.boot.java.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ide.vscode.commons.boot.app.cli.SpringBootApp;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBean;
import org.springframework.ide.vscode.commons.boot.app.cli.livebean.LiveBeansModel;

/**
 * @author Martin Lippert
 */
public class ChangeHistory {

	private static final List<LiveBean> EMPTY_BEANS_LIST = new ArrayList<>(0);

	private final String virtualAppID;
	
	private SpringBootApp runningProcess;
	private LiveBeansModel lastBeans;

	public ChangeHistory(String virtualAppID) {
		this.virtualAppID = virtualAppID;
	}
	
	public void updateProcess(SpringBootApp app) {
		this.runningProcess = app;
	}

	public Change checkForUpdates() {
		Change result = null;

		LiveBeansModel currentBeans = this.runningProcess.getBeans();
		if (!currentBeans.isEmpty()) {

			if (lastBeans == null) {
				lastBeans = currentBeans;
			}
			else if (lastBeans != null && currentBeans != null) {
				result = calculateBeansDiff(lastBeans, currentBeans, result);
				lastBeans = currentBeans;
			}
		}
		
		return result;
	}

	private Change calculateBeansDiff(LiveBeansModel previous, LiveBeansModel current, Change result) {
		if (previous == current) {
			return result;
		}
		
		Set<String> currentNames = current.getBeanNames();
		Set<String> previousNames = previous.getBeanNames();
		
		Set<String> allNames = new HashSet<>(currentNames);
		allNames.addAll(previousNames);
		
		for (String name : allNames) {
			List<LiveBean> currentBeans = current.getBeansOfName(name);
			List<LiveBean> previousBeans = previous.getBeansOfName(name);
			
			result = calculateBeansDiff(previousBeans, currentBeans, result);
		}
		
		return result;
	}

	private Change calculateBeansDiff(List<LiveBean> previousBeans, List<LiveBean> currentBeans, Change result) {
		if (currentBeans == null) currentBeans = EMPTY_BEANS_LIST;
		if (previousBeans == null) previousBeans = EMPTY_BEANS_LIST;

		for (LiveBean bean : previousBeans) {
			if (!contains(currentBeans, bean)) {

				if (result == null) {
					result = new Change();
				}

				result.addDeletedBean(bean);
			}
		}
		
		for (LiveBean bean : currentBeans) {
			if (!contains(previousBeans, bean)) {
				
				if (result == null) {
					result = new Change();
				}

				result.addNewBean(bean);
			}
		}
		
		return result;
	}

	private boolean contains(List<LiveBean> beans, LiveBean bean) {
		for (LiveBean beansFromList : beans) {
			if (StringUtils.equals(beansFromList.getId(), bean.getId())
					&& StringUtils.equals(beansFromList.getType(), bean.getType())
					&& StringUtils.equals(beansFromList.getResource(), bean.getResource())) {
				return true;
			}
		}
		
		return false;
	}

}
