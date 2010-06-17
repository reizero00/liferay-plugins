/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.workflow.WorkflowException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignment;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskAssignmentInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoTaskInstanceToken;
import com.liferay.portal.workflow.kaleo.service.base.KaleoTaskAssignmentInstanceLocalServiceBaseImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * <a href="KaleoTaskAssignmentInstanceLocalServiceImpl.java.html"><b><i>View
 * Source</i></b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class KaleoTaskAssignmentInstanceLocalServiceImpl
	extends KaleoTaskAssignmentInstanceLocalServiceBaseImpl {

	public KaleoTaskAssignmentInstance addKaleoTaskAssignmentInstance(
			KaleoTaskInstanceToken kaleoTaskInstanceToken,
			String assigneeClassName, long assigneeClassPK,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		User user = userPersistence.findByPrimaryKey(
						serviceContext.getUserId());

		long kaleoTaskAssignmentInstanceId = counterLocalService.increment();

		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance =
			kaleoTaskAssignmentInstancePersistence.create(
				kaleoTaskAssignmentInstanceId);

		populateKaleoTaskAssignmentInstance(
			kaleoTaskAssignmentInstance, kaleoTaskInstanceToken, user);

		kaleoTaskAssignmentInstance.setAssigneeClassName(assigneeClassName);
		kaleoTaskAssignmentInstance.setAssigneeClassPK(assigneeClassPK);

		kaleoTaskAssignmentInstancePersistence.update(
			kaleoTaskAssignmentInstance, false);

		return kaleoTaskAssignmentInstance;
	}

	public List<KaleoTaskAssignmentInstance> addTaskAssignmentInstances(
			KaleoTaskInstanceToken kaleoTaskInstanceToken,
			Collection<KaleoTaskAssignment> kaleoTaskAssignments,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		List<KaleoTaskAssignmentInstance> kaleoTaskAssignmentInstances =
			new ArrayList<KaleoTaskAssignmentInstance>(
				kaleoTaskAssignments.size());

		for (KaleoTaskAssignment kaleoTaskAssignment : kaleoTaskAssignments) {
			User user = userPersistence.findByPrimaryKey(
						serviceContext.getUserId());

			long kaleoTaskAssignmentInstanceId = counterLocalService.increment();

			KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance =
				kaleoTaskAssignmentInstancePersistence.create(
					kaleoTaskAssignmentInstanceId);

			populateKaleoTaskAssignmentInstance(
				kaleoTaskAssignmentInstance, kaleoTaskInstanceToken, user);

			kaleoTaskAssignmentInstance.setAssigneeClassName(
				kaleoTaskAssignment.getAssigneeClassName());

			if ((kaleoTaskAssignment.getAssigneeClassPK() == 0) &&
				User.class.getName().equals(
					kaleoTaskAssignment.getAssigneeClassName())) {

				kaleoTaskAssignmentInstance.setAssigneeClassPK(
					kaleoTaskInstanceToken.getUserId());
			}
			else {
				kaleoTaskAssignmentInstance.setAssigneeClassPK(
					kaleoTaskAssignment.getAssigneeClassPK());
			}

			kaleoTaskAssignmentInstancePersistence.update(
				kaleoTaskAssignmentInstance, false);

			kaleoTaskAssignmentInstances.add(kaleoTaskAssignmentInstance);
		}

		return kaleoTaskAssignmentInstances;
	}

	public KaleoTaskAssignmentInstance assignKaleoTaskAssignmentInstance(
			KaleoTaskInstanceToken kaleoTaskInstanceToken,
			String assigneeClassName, long assigneeClassPK,
			ServiceContext serviceContext)
	throws PortalException, SystemException {

		deleteKaleoTaskAssignmentInstances(kaleoTaskInstanceToken);

		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance =
			addKaleoTaskAssignmentInstance(
				kaleoTaskInstanceToken, assigneeClassName,
				assigneeClassPK, serviceContext);

		return kaleoTaskAssignmentInstance;
	}

	public KaleoTaskAssignmentInstance completeKaleoTaskInstanceToken(
			long kaleoTaskInstanceTokenId, ServiceContext serviceContext)
		throws PortalException, SystemException {

		List<KaleoTaskAssignmentInstance> kaleoTaskAssignmentInstances =
			kaleoTaskAssignmentInstancePersistence.
				findBykaleoTaskInstanceTokenId(
					kaleoTaskInstanceTokenId);

		if (kaleoTaskAssignmentInstances.size() > 1) {
			throw new WorkflowException(
				"Cannot complete a task that is not " +
				"assigned to an individual user.");
		}

		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance =
			kaleoTaskAssignmentInstances.get(0);

		kaleoTaskAssignmentInstance.setCompleted(true);
		kaleoTaskAssignmentInstance.setCompletionDate(new Date());

		kaleoTaskAssignmentInstancePersistence.update(
			kaleoTaskAssignmentInstance, false);

		return kaleoTaskAssignmentInstance;
	}

	public void deleteKaleoTaskAssignmentInstances(
			KaleoTaskInstanceToken kaleoTaskInstanceToken)
		throws PortalException, SystemException {

		List<KaleoTaskAssignmentInstance> kaleoTaskAssignmentInstances =
			kaleoTaskAssignmentInstancePersistence.
				findBykaleoTaskInstanceTokenId(
					kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());

		for (KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance :
				kaleoTaskAssignmentInstances) {

			kaleoTaskAssignmentInstancePersistence.remove(
				kaleoTaskAssignmentInstance);
		}
	}

	public void deleteKaleoTaskAssignmentInstances(long kaleoDefintionId)
		throws SystemException {

		kaleoTaskAssignmentInstancePersistence.removeByKaleoDefinitionId(
			kaleoDefintionId);
	}

	public void deleteKaleoTaskAssignmentInstancesByKaleoInstanceId(
			long kaleoInstanceId)
		throws SystemException {

		kaleoTaskAssignmentInstancePersistence.removeByKaleoInstanceId(
			kaleoInstanceId);
	}

	public List<KaleoTaskAssignmentInstance> getKaleoTaskAssignmentInstances(
			long kaleoTaskInstanceTokenId)
		throws SystemException {

		return kaleoTaskAssignmentInstancePersistence.
			findBykaleoTaskInstanceTokenId(kaleoTaskInstanceTokenId);
	}

	protected void populateKaleoTaskAssignmentInstance(
		KaleoTaskAssignmentInstance kaleoTaskAssignmentInstance,
		KaleoTaskInstanceToken kaleoTaskInstanceToken, User user) {

		Date now = new Date();

		kaleoTaskAssignmentInstance.setCompanyId(user.getCompanyId());
		kaleoTaskAssignmentInstance.setUserId(user.getUserId());
		kaleoTaskAssignmentInstance.setUserName(user.getFullName());
		kaleoTaskAssignmentInstance.setCreateDate(now);
		kaleoTaskAssignmentInstance.setModifiedDate(now);
		kaleoTaskAssignmentInstance.setCompleted(false);

		kaleoTaskAssignmentInstance.setGroupId(
			kaleoTaskInstanceToken.getGroupId());
		kaleoTaskAssignmentInstance.setKaleoDefinitionId(
			kaleoTaskInstanceToken.getKaleoDefinitionId());
		kaleoTaskAssignmentInstance.setKaleoInstanceId(
			kaleoTaskInstanceToken.getKaleoInstanceId());
		kaleoTaskAssignmentInstance.setKaleoTaskInstanceTokenId(
			kaleoTaskInstanceToken.getKaleoTaskInstanceTokenId());
		kaleoTaskAssignmentInstance.setKaleoTaskName(
			kaleoTaskInstanceToken.getKaleoTaskName());
	}
}