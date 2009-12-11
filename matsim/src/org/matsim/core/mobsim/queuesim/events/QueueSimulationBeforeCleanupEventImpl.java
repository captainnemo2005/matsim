/* *********************************************************************** *
 * project: org.matsim.*
 * QueueSimulationBeforeCleanupEventImpl
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.core.mobsim.queuesim.events;

import org.matsim.core.mobsim.Simulation;


/**
 * @author dgrether
 * @see QueueSimulationBeforeCleanupEvent
 */
public class QueueSimulationBeforeCleanupEventImpl<T extends Simulation> extends
		AbstractQueueSimulationEvent<T> implements QueueSimulationBeforeCleanupEvent<T> {

	public QueueSimulationBeforeCleanupEventImpl(T queuesim) {
		super(queuesim);
	}

}
