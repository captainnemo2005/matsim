package playground.michalm.vrp.run;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.matsim.analysis.LegHistogram;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.mobsim.qsim.ActivityEngine;
import org.matsim.core.mobsim.qsim.QSim;
import org.matsim.core.mobsim.qsim.TeleportationEngine;
import org.matsim.core.mobsim.qsim.agents.DefaultAgentFactory;
import org.matsim.core.mobsim.qsim.agents.PopulationAgentSource;
import org.matsim.core.mobsim.qsim.qnetsimengine.DefaultQSimEngineFactory;
import org.matsim.core.mobsim.qsim.qnetsimengine.QNetsimEngine;
import org.matsim.core.network.MatsimNetworkReader;
import org.matsim.core.population.MatsimPopulationReader;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import pl.poznan.put.util.lang.TimeDiscretizer;
import pl.poznan.put.vrp.dynamic.data.VrpData;
import pl.poznan.put.vrp.dynamic.data.model.Customer;
import pl.poznan.put.vrp.dynamic.data.model.Request;
import pl.poznan.put.vrp.dynamic.data.network.ArcFactory;
import pl.poznan.put.vrp.dynamic.optimizer.taxi.TaxiOptimizer;
import playground.michalm.demand.ODDemandGenerator;
import playground.michalm.vrp.data.MatsimVrpData;
import playground.michalm.vrp.data.file.DepotReader;
import playground.michalm.vrp.data.network.MatsimVrpGraph;
import playground.michalm.vrp.data.network.MatsimVrpGraphCreator;
import playground.michalm.vrp.data.network.router.DistanceAsTravelDisutility;
import playground.michalm.vrp.data.network.router.TimeAsTravelDisutility;
import playground.michalm.vrp.data.network.router.TravelTimeCalculators;
import playground.michalm.vrp.data.network.shortestpath.MatsimArcFactories;
import playground.michalm.vrp.taxi.TaxiAgentSource;
import playground.michalm.vrp.taxi.TaxiModeDepartureHandler;
import playground.michalm.vrp.taxi.TaxiSimEngine;


public class OnlineDvrpLauncherUtils
{
    public enum TravelTimeSource
    {
        FREE_FLOW_SPEED("FF", 24 * 60 * 60), // no eventsFileName
        EVENTS_24_H("24H", 24 * 60 * 60), // based on eventsFileName, with 24-hour time interval
        EVENTS_15_MIN("15M", 15 * 60); // based on eventsFileName, with 15-minute time interval

        /*package*/final String shortcut;
        /*package*/final int travelTimeBinSize;
        /*package*/final int numSlots;


        private TravelTimeSource(String shortcut, int travelTimeBinSize)
        {
            this.shortcut = shortcut;
            this.travelTimeBinSize = travelTimeBinSize;
            this.numSlots = 24 * 60 * 60 / travelTimeBinSize;// to cover 24 hours
        }
    }


    public enum TravelCostSource
    {
        TIME, // travel time
        DISTANCE; // travel distance
    }


    /**
     * Mandatory
     */
    public static Scenario initMatsimData(String netFileName, String plansFileName,
            String taxiCustomersFileName)
    {
        Scenario scenario = ScenarioUtils.createScenario(VrpConfigUtils.createConfig());

        new MatsimNetworkReader(scenario).readFile(netFileName);
        new MatsimPopulationReader(scenario).readFile(plansFileName);

        List<String> taxiCustomerIds;
        try {
            taxiCustomerIds = ODDemandGenerator.readTaxiCustomerIds(taxiCustomersFileName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (String id : taxiCustomerIds) {
            Person person = scenario.getPopulation().getPersons().get(scenario.createId(id));
            Leg leg = (Leg)person.getSelectedPlan().getPlanElements().get(1);
            leg.setMode(TaxiModeDepartureHandler.TAXI_MODE);
        }
        
// replacing the above fore loop by the code below will remove	 the non-taxicab passengers from the simulation.
// was, for example, useful for a "freight-like" demo.
//        Collection<Id> normalPersons = new ArrayList<Id>() ;
//        for ( Entry<Id, ? extends Person> entry : scenario.getPopulation().getPersons().entrySet() ) {
//        	Person person = entry.getValue() ;
//        	if ( taxiCustomerIds.contains( person.getId().toString() ) ) {
//              Leg leg = (Leg)person.getSelectedPlan().getPlanElements().get(1);
//              leg.setMode(TaxiModeDepartureHandler.TAXI_MODE);
//        	} else {
//        		normalPersons.add( person.getId() ) ;
//        	}
//        }
//        System.err.println( " population size before deletion: " + scenario.getPopulation().getPersons().size() );
//        for ( Id id : normalPersons ) {
//        	scenario.getPopulation().getPersons().remove(id) ;
//        }
//        System.err.println( " population size after deletion: " + scenario.getPopulation().getPersons().size() );
        
        return scenario;
    }


    /**
     * Mandatory
     */
    public static MatsimVrpData initMatsimVrpData(Scenario scenario, TravelTimeSource ttimeSource,
            TravelCostSource tcostSource, String eventsFileName, String depotsFileName)
    {
        int travelTimeBinSize = ttimeSource.travelTimeBinSize;
        int numSlots = ttimeSource.numSlots;

        scenario.getConfig().travelTimeCalculator().setTraveltimeBinSize(travelTimeBinSize);

        TravelTime ttimeCalc;
        TravelDisutility tcostCalc;

        switch (ttimeSource) {
            case FREE_FLOW_SPEED:
                ttimeCalc = new FreeSpeedTravelTime();
                break;

            case EVENTS_15_MIN:
            case EVENTS_24_H:
                ttimeCalc = TravelTimeCalculators.createTravelTimeFromEvents(eventsFileName,
                        scenario);
                break;

            default:
                throw new IllegalArgumentException();
        }

        switch (tcostSource) {
            case DISTANCE:
                tcostCalc = new DistanceAsTravelDisutility();
                break;

            case TIME:
                tcostCalc = new TimeAsTravelDisutility(ttimeCalc);
                break;

            default:
                throw new IllegalArgumentException();
        }

        Network network = scenario.getNetwork();
        TimeDiscretizer timeDiscretizer = new TimeDiscretizer(travelTimeBinSize, numSlots);
        ArcFactory arcFactory = MatsimArcFactories.createArcFactory(network, ttimeCalc, tcostCalc,
                timeDiscretizer, false);

        MatsimVrpGraph graph;
        try {
            graph = MatsimVrpGraphCreator.create(network, arcFactory, false);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        VrpData vrpData = new VrpData();
        vrpData.setVrpGraph(graph);
        vrpData.setCustomers(new ArrayList<Customer>());
        vrpData.setRequests(new ArrayList<Request>());
        new DepotReader(scenario, vrpData).readFile(depotsFileName);

        return new MatsimVrpData(vrpData, scenario);
    }


    /**
     * Mandatory
     */
    public static QSim initQSim(MatsimVrpData data, TaxiOptimizer optimizer,
            boolean onlineVehicleTracker)
    {
        Scenario scenario = data.getScenario();
        EventsManager events = EventsUtils.createEventsManager();
        QSim qSim = new QSim(scenario, events);

        ActivityEngine activityEngine = new ActivityEngine();
        qSim.addMobsimEngine(activityEngine);
        qSim.addActivityHandler(activityEngine);

        QNetsimEngine netsimEngine = new DefaultQSimEngineFactory().createQSimEngine(qSim);
        qSim.addMobsimEngine(netsimEngine);
        qSim.addDepartureHandler(netsimEngine.getDepartureHandler());

        TeleportationEngine teleportationEngine = new TeleportationEngine();
        qSim.addMobsimEngine(teleportationEngine);

        TaxiSimEngine taxiSimEngine = new TaxiSimEngine(qSim, data, optimizer);
        qSim.addMobsimEngine(taxiSimEngine);

        qSim.addAgentSource(new PopulationAgentSource(scenario.getPopulation(),
                new DefaultAgentFactory(qSim), qSim));
        qSim.addAgentSource(new TaxiAgentSource(data, taxiSimEngine, onlineVehicleTracker));
        qSim.addDepartureHandler(new TaxiModeDepartureHandler(taxiSimEngine, data));

        return qSim;
    }


    /**
     * Optional
     */
    public static void writeHistograms(LegHistogram legHistogram, String histogramOutDirName)
    {
        new File(histogramOutDirName).mkdir();
        legHistogram.write(histogramOutDirName + "legHistogram_all.txt");
        legHistogram.writeGraphic(histogramOutDirName + "legHistogram_all.png");
        for (String legMode : legHistogram.getLegModes()) {
            legHistogram.writeGraphic(histogramOutDirName + "legHistogram_" + legMode + ".png",
                    legMode);
        }
    }
}
