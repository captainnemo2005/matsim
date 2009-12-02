/* *********************************************************************** *
 * project: org.matsim.*
 * PlanomatOptimizeTimes.java
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

package org.matsim.core.replanning.modules;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.groups.PlanomatConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.router.PlansCalcRoute;
import org.matsim.core.router.util.TravelCost;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.planomat.Planomat;
import org.matsim.planomat.costestimators.DepartureDelayAverageCalculator;
import org.matsim.planomat.costestimators.LegTravelTimeEstimatorFactory;
import org.matsim.population.algorithms.PlanAlgorithm;

/**
 * This class is just a multithreading wrapper for instances of the
 * optimizing plan algorithm which is usually called "planomat".
 *
 * @author meisterk
 */
public class PlanomatModule extends AbstractMultithreadedModule {

	private final TravelCost travelCost;
	private final TravelTime travelTime;
	private final ScoringFunctionFactory scoringFunctionFactory;
	private final Controler controler;
	private final PlanomatConfigGroup config;
	private final LegTravelTimeEstimatorFactory legTravelTimeEstimatorFactory;
	
	private DepartureDelayAverageCalculator tDepDelayCalc = null;

	public PlanomatModule(
			Controler controler, 
			EventsManager events, 
			Network network,
			ScoringFunctionFactory scoringFunctionFactory,
			TravelCost travelCost, 
			TravelTime travelTime) {
		super();
		this.controler = controler;
		this.scoringFunctionFactory = scoringFunctionFactory;
		this.travelCost = travelCost;
		this.travelTime = travelTime;

		this.config = controler.getConfig().planomat();
		
		this.tDepDelayCalc = new DepartureDelayAverageCalculator(
				network,
				controler.getConfig().travelTimeCalculator().getTraveltimeBinSize());
		events.addHandler(tDepDelayCalc);

		this.legTravelTimeEstimatorFactory = new LegTravelTimeEstimatorFactory(this.travelTime, this.tDepDelayCalc);
	}

	@Override
	public PlanAlgorithm getPlanAlgoInstance() {

		/*
		 * TODO Instances of a routing algorithm should be generated by a factory rather than from a method in the controler. 
		 * The controler is not required here for anything else.
		 */
		PlansCalcRoute routingAlgorithm = (PlansCalcRoute) this.controler.getRoutingAlgorithm(this.travelCost, this.travelTime);
		
		PlanAlgorithm planomatInstance = new Planomat(
				this.legTravelTimeEstimatorFactory,
				this.scoringFunctionFactory, 
				this.config,
				routingAlgorithm);

		return planomatInstance;

	}
	
}
