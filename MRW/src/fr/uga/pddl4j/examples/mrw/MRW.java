/*
 * Copyright (c) 2021 by Damien Pellier <Damien.Pellier@imag.fr>.
 *
 * This file is part of PDDL4J library.
 *
 * PDDL4J is free software: you can redistribute it and/or modify * it under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * PDDL4J is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License * along with PDDL4J.  If not,
 * see <http://www.gnu.org/licenses/>
 */

package fr.uga.pddl4j.examples.mrw;

import fr.uga.pddl4j.heuristics.state.StateHeuristic;
import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.RequireKey;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.planners.PlannerConfiguration;
import fr.uga.pddl4j.planners.ProblemNotSupportedException;
import fr.uga.pddl4j.planners.SearchStrategy;
import fr.uga.pddl4j.planners.statespace.search.StateSpaceSearch;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.State;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.ArrayList;
import java.util.Random;


@CommandLine.Command(name = "MRW",
    version = "MRW 1.0",
    description = "Solves a specified planning problem using MRW search strategy.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class MRW extends AbstractPlanner {
    // Custom class to hold an index and an action
    private static class ActionIndexPair {
        int index;
        Action action;

        ActionIndexPair(int index, Action action) {
            this.index = index;
            this.action = action;
        }
    }
    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(MRW.class.getName());

    /**
     * The HEURISTIC property used for planner configuration.
     */
    public static final String HEURISTIC_SETTING = "HEURISTIC";

    /**
     * The default value of the HEURISTIC property used for planner configuration.
     */
    public static final StateHeuristic.Name DEFAULT_HEURISTIC = StateHeuristic.Name.FAST_FORWARD;

    /**
     * The WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final String WEIGHT_HEURISTIC_SETTING = "WEIGHT_HEURISTIC";

    /**
     * The default value of the WEIGHT_HEURISTIC property used for planner configuration.
     */
    public static final double DEFAULT_WEIGHT_HEURISTIC = 1.0;

    /**
     * The weight of the heuristic.
     */
    private double heuristicWeight;

    /**
     * The name of the heuristic used by the planner.
     */
    private StateHeuristic.Name heuristic;

    /**
     * Creates a new MRW search planner with the default configuration.
     */
    public MRW() {
        this(MRW.getDefaultConfiguration());
    }

    /**
     * Creates a new MRW search planner with a specified configuration.
     *
     * @param configuration the configuration of the planner.
     */
    public MRW(final PlannerConfiguration configuration) {
        super();
        this.setConfiguration(configuration);
    }

    /**
     * Sets the weight of the heuristic.
     *
     * @param weight the weight of the heuristic. The weight must be greater than 0.
     * @throws IllegalArgumentException if the weight is strictly less than 0.
     */
    @CommandLine.Option(names = {"-w", "--weight"}, defaultValue = "1.0",
        paramLabel = "<weight>", description = "Set the weight of the heuristic (preset 1.0).")
    public void setHeuristicWeight(final double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight <= 0");
        }
        this.heuristicWeight = weight;
    }

    /**
     * Set the name of heuristic used by the planner to the solve a planning problem.
     *
     * @param heuristic the name of the heuristic.
     */
    @CommandLine.Option(names = {"-e", "--heuristic"},
        description = "Set the heuristic : AJUSTED_SUM, AJUSTED_SUM2, AJUSTED_SUM2M, COMBO, "
            + "MAX, FAST_FORWARD SET_LEVEL, SUM, SUM_MUTEX (preset: FAST_FORWARD)")
    public void setHeuristic(StateHeuristic.Name heuristic) {
        this.heuristic = heuristic;
    }

    /**
     * Returns the name of the heuristic used by the planner to solve a planning problem.
     *
     * @return the name of the heuristic used by the planner to solve a planning problem.
     */
    public final StateHeuristic.Name getHeuristic() {
        return this.heuristic;
    }

    /**
     * Returns the weight of the heuristic.
     *
     * @return the weight of the heuristic.
     */
    public final double getHeuristicWeight() {
        return this.heuristicWeight;
    }

    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Search a solution plan to a specified domain and problem using MRW.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problem) throws ProblemNotSupportedException {
        LOGGER.info("* Starting MRW search \n");
        // Search a solution
        final long begin = System.currentTimeMillis();
        // final Plan plan = this.astar(problem); // No error now because exception is declared in method signature
        // final Plan plan = this.MRW(problem); // No error now because exception is declared in method signature
        final Plan plan = this.MRW(problem); // No error now because exception is declared in method signature
        final long end = System.currentTimeMillis();
        // If a plan is found update the statistics of the planner
        // and log search information
        if (plan != null) {
            LOGGER.info("* MRW search succeeded\n");
            this.getStatistics().setTimeToSearch(end - begin);
        } else {
            LOGGER.info("* MRW search failed\n");
        }
        // Return the plan found or null if the search fails.
        return plan;
    }
    /**
     * Checks the planner configuration and returns if the configuration is valid.
     * A configuration is valid if (1) the domain and the problem files exist and
     * can be read, (2) the timeout is greater than 0, (3) the weight of the
     * heuristic is greater than 0 and (4) the heuristic is a not null.
     *
     * @return <code>true</code> if the configuration is valid <code>false</code> otherwise.
     */
    public boolean hasValidConfiguration() {
        return super.hasValidConfiguration()
            && this.getHeuristicWeight() > 0.0
            && this.getHeuristic() != null;
    }

    /**
     * This method return the default arguments of the planner.
     *
     * @return the default arguments of the planner.
     * @see PlannerConfiguration
     */
    public static PlannerConfiguration getDefaultConfiguration() {
        PlannerConfiguration config = Planner.getDefaultConfiguration();
        config.setProperty(MRW.HEURISTIC_SETTING, MRW.DEFAULT_HEURISTIC.toString());
        config.setProperty(MRW.WEIGHT_HEURISTIC_SETTING, Double.toString(MRW.DEFAULT_WEIGHT_HEURISTIC));
        config.setProperty("NUM_WALK", Integer.toString(10000)); // Add this line
        config.setProperty("LENGTH_WALK", Integer.toString(20));  // Add this line
        return config;
    }

    /**
     * Returns the configuration of the planner.
     *
     * @return the configuration of the planner.
     */
    @Override
    public PlannerConfiguration getConfiguration() {
        final PlannerConfiguration config = super.getConfiguration();
        config.setProperty(MRW.HEURISTIC_SETTING, this.getHeuristic().toString());
        config.setProperty(MRW.WEIGHT_HEURISTIC_SETTING, Double.toString(this.getHeuristicWeight()));
        config.setProperty("NUM_WALK", Integer.toString(this.numWalk));  // Add this line
        config.setProperty("LENGTH_WALK", Integer.toString(this.lengthWalk));  // Add this line
        return config;
    }

    /**
     * Sets the configuration of the planner. If a planner setting is not defined in
     * the specified configuration, the setting is initialized with its default value.
     *
     * @param configuration the configuration to set.
     */
    @Override
    public void setConfiguration(final PlannerConfiguration configuration) {
        super.setConfiguration(configuration);
        if (configuration.getProperty(MRW.WEIGHT_HEURISTIC_SETTING) == null) {
            this.setHeuristicWeight(MRW.DEFAULT_WEIGHT_HEURISTIC);
        } else {
            this.setHeuristicWeight(Double.parseDouble(configuration.getProperty(
                MRW.WEIGHT_HEURISTIC_SETTING)));
        }
        if (configuration.getProperty(MRW.HEURISTIC_SETTING) == null) {
            this.setHeuristic(MRW.DEFAULT_HEURISTIC);
        } else {
            this.setHeuristic(StateHeuristic.Name.valueOf(configuration.getProperty(
                MRW.HEURISTIC_SETTING)));
        }
        // Set numWalk and lengthWalk if present
        if (configuration.getProperty("NUM_WALK") != null) {
            this.numWalk = Integer.parseInt(configuration.getProperty("NUM_WALK"));
        }
        if (configuration.getProperty("LENGTH_WALK") != null) {
            this.lengthWalk = Integer.parseInt(configuration.getProperty("LENGTH_WALK"));
        }
    }

    /**
     * The main method of the <code>MRW</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            System.out.println("this is Monte Carlo PRW");
            final MRW planner = new MRW();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }

    /**
     * Search a solution plan for a planning problem using an MRW search strategy.
     *
     * @param problem the problem to solve.
     * @return a plan solution for the problem or null if there is no solution
     * @throws ProblemNotSupportedException if the problem to solve is not supported by the planner.
     */


    /**
     * The number of random walks.
     */
    @CommandLine.Option(names = {"-n", "--numWalk"}, defaultValue = "10000",
        paramLabel = "<numWalk>", description = "Set the number of random walks (preset 10000).")
    private int numWalk;

    /**
     * The length of each random walk.
     */
    @CommandLine.Option(names = {"-lw", "--lengthWalk"}, defaultValue = "20",
        paramLabel = "<lengthWalk>", description = "Set the length of each random walk (preset 20).")
    private int lengthWalk;


    public Plan MRW(Problem problem) throws ProblemNotSupportedException {
        // Check if the problem is supported by the planner
        System.out.println("Hello, world! this is Monte Carlo PRW");
        if (!this.isSupported(problem)) {
            throw new ProblemNotSupportedException("Problem not supported");
        }

        // We create an instance of the heuristic to use to guide the search
        final StateHeuristic heuristic = StateHeuristic.getInstance(this.getHeuristic(), problem);

        // We get the initial state from the planning problem
        final State init = new State(problem.getInitialState());

        // Initialize variables for the pure random walk
        double hmin = Double.POSITIVE_INFINITY;
        Node smin = null;

        // We start the random walks
        Node sinit = new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal()));

        for (int i = 1; i <= numWalk; i++) {
            Node s = new Node(init, null, -1, 0, heuristic.estimate(init, problem.getGoal()));
            for (int j = 1; j <= lengthWalk; j++) {

                // Get applicable actions for the current state
                List<ActionIndexPair> applicableActions = new ArrayList<>();
                for (int k = 0; k < problem.getActions().size(); k++) {
                    Action a = problem.getActions().get(k);
                    if (a.isApplicable(s)) {
                        applicableActions.add(new ActionIndexPair(k, a));
                    }
                }

                // If no actions are applicable, break out of the loop
                if (applicableActions.isEmpty()) {
                    break;
                }

                // Select a random action and apply it
                int index = new Random().nextInt(applicableActions.size());
                ActionIndexPair randomPair = applicableActions.get(index);
                Action randomAction = randomPair.action;
                int actionIndex = randomPair.index;

                Node next = new Node(s);
                for (ConditionalEffect ce : randomAction.getConditionalEffects()) {
                    if (s.satisfy(ce.getCondition())) {
                        next.apply(ce.getEffect());
                    }
                }

                // Update the state to the new state after applying the action
                next.setParent(s);
                next.setAction(actionIndex);  // Set the correct action index
                next.setHeuristic(heuristic.estimate(next, problem.getGoal()));

                // If the goal is satisfied, return the plan
                s = next;
                if (s.satisfy(problem.getGoal())) {
                    System.out.println("this state satisfies the goal");
                    System.out.println(s.satisfy(problem.getGoal()));
                    System.out.println("this is num walk:");
                    System.out.println(i);
                    return this.extractPlan(s, problem);
                }
            }

            // Update smin if this walk has found a better state
            double h = heuristic.estimate(s, problem.getGoal());
            if (h < hmin) {
                smin = s;
                hmin = h;
            }
        }

        // If no better state was found, return null or an empty plan
        if (smin == null) {
            return this.extractPlan(sinit, problem);
            // return null;
        } else {
            return this.extractPlan(smin, problem);
        }
    }

    /**
     * Extracts a search from a specified node.
     *
     * @param node    the node.
     * @param problem the problem.
     * @return the search extracted from the specified node.
     */
    private Plan extractPlan(final Node node, final Problem problem) {
        Node n = node;

        final Plan plan = new SequentialPlan();
        while (n.getAction() != -1) {
            final Action a = problem.getActions().get(n.getAction());
            plan.add(0, a);

            // Check if n has a parent before accessing it
            if (n.getParent() != null) {
                n = n.getParent();
            } else {
                break;  // Exit the loop if there's no parent (i.e., we've reached the root)
            }

        }

        return plan;
    }

    /**
     * Returns if a specified problem is supported by the planner. Just ADL problem can be solved by this planner.
     *
     * @param problem the problem to test.
     * @return <code>true</code> if the problem is supported <code>false</code> otherwise.
     */
    @Override
    public boolean isSupported(Problem problem) {
        return (problem.getRequirements().contains(RequireKey.ACTION_COSTS)
            || problem.getRequirements().contains(RequireKey.CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.CONTINOUS_EFFECTS)
            || problem.getRequirements().contains(RequireKey.DERIVED_PREDICATES)
            || problem.getRequirements().contains(RequireKey.DURATIVE_ACTIONS)
            || problem.getRequirements().contains(RequireKey.DURATION_INEQUALITIES)
            || problem.getRequirements().contains(RequireKey.FLUENTS)
            || problem.getRequirements().contains(RequireKey.GOAL_UTILITIES)
            || problem.getRequirements().contains(RequireKey.METHOD_CONSTRAINTS)
            || problem.getRequirements().contains(RequireKey.NUMERIC_FLUENTS)
            || problem.getRequirements().contains(RequireKey.OBJECT_FLUENTS)
            || problem.getRequirements().contains(RequireKey.PREFERENCES)
            || problem.getRequirements().contains(RequireKey.TIMED_INITIAL_LITERALS)
            || problem.getRequirements().contains(RequireKey.HIERARCHY))
            ? false : true;
    }
}