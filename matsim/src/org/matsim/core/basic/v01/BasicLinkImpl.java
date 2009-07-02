/* *********************************************************************** *
 * project: org.matsim.*
 * BasicLink.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.core.basic.v01;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.EnumSet;
import java.util.Set;

import org.matsim.api.basic.v01.Coord;
import org.matsim.api.basic.v01.Id;
import org.matsim.api.basic.v01.TransportMode;
import org.matsim.api.basic.v01.network.BasicLink;
import org.matsim.api.basic.v01.network.BasicNode;
import org.matsim.core.network.NetworkLayer;
import org.matsim.core.utils.geometry.CoordImpl;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.world.AbstractLocation;

public class BasicLinkImpl
// <L extends BasicLinkI, N extends BasicNodeI > // and then change all BasicLink/BasicNode below into L and N
extends AbstractLocation // yyyyyy ????
implements BasicLink
{
	protected BasicNode from = null;
	protected BasicNode to = null;

	protected double length = Double.NaN;
	protected double freespeed = Double.NaN;
	protected double capacity = Double.NaN;
	protected double nofLanes = Double.NaN;
	
	protected EnumSet<TransportMode> allowedModes = EnumSet.of(TransportMode.car);

	// TODO [balmermi] A link exists only iff a to- and a from node is defined.
	// Furthermore: Since a BasicLink is a location, and a location is a geographic
	// object, the BasicLink must contains geographic info. Since this must be defined
	// by the to- and from-node, they HAVE to contain a coordinate. (see also BasicNode)
	// If this is not O.K., then the BasicLink must not extend Location.
	protected BasicLinkImpl(final NetworkLayer network, final Id id, final BasicNode from, final BasicNode to) {
		super(network, id, 
				new CoordImpl(0.5*(from.getCoord().getX() + to.getCoord().getX()), 0.5*(from.getCoord().getY() + to.getCoord().getY()))
		);
		this.from = from;
		this.to = to;
	}

	// TODO [balmermi] For simplicity, we calculate only the distance to the center
	// of that link. A better version is implemented in org.matsim.demandmodeling.network.Link.
	// It would be better to implement the version in Link here and remove the one in Link.
	@Override
	public double calcDistance(final Coord coord) {
		return CoordUtils.calcDistance(this.center, coord);
	}

	public BasicNode getFromNode() { // not final since type needs to be overwritten
		return this.from;
	}

	public BasicNode getToNode() { // not final since type needs to be overwritten
		return this.to;
	}

	public final boolean setFromNode(final BasicNode node) {
		this.from = node;
		return true;
	}

	public final boolean setToNode(final BasicNode node) {
		this.to = node;
		return true;
	}
	/**
	 * This method returns the capacity as set in the xml defining the network. Be aware
	 * that this capacity is not normalized in time, it depends on the period set
	 * in the network file (the capperiod attribute).
	 *
 	 * @param time - the current time
	 * @return the capacity per network's capperiod timestep
	 */
	public double getCapacity(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.capacity;
	}

	public final void setCapacity(final double capacity) { // NOT needed in TimeVariantLinkImpl.  Sets "base" capacity (time-indep)
		this.capacity = capacity;
	}
	/**
	 * This method returns the freespeed velocity in meter per seconds.
	 *
	 * @param time - the current time
	 * @return freespeed
	 */
	public double getFreespeed(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.freespeed;
	}
	/**
	 * Sets the freespeed velocity of the link in meter per seconds.
	 */
	public final void setFreespeed(final double freespeed) {
		this.freespeed = freespeed;
	}

	public final double getLength() {
		return this.length;
	}

	public final void setLength(final double length) {
		this.length = length;
	}

	public double getNumberOfLanes(final double time) { // not final since needed in TimeVariantLinkImpl
		return this.nofLanes;
	}

	public final void setNumberOfLanes(final double lanes) {
		this.nofLanes = lanes;
	}
	
	public final void setAllowedModes(final Set<TransportMode> modes) {
		this.allowedModes.clear();
		this.allowedModes.addAll(modes);
	}
	
	public final Set<TransportMode> getAllowedModes() {
		return this.allowedModes.clone();
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		this.from.addOutLink(this);
		this.to.addInLink(this);
	}

}
