package fr.uga.pddl4j.planners.sat;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.parser.ErrorManager;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.planners.Planner;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.Operator;
import fr.uga.pddl4j.util.LogLevel;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SAT Planner implementation using SAT4J.
 */
public class SatPlanner implements Planner {

    private String domainPath;
    private String problemPath;
    private int timeout = DEFAULT_TIME_OUT;
    private LogLevel logLevel = DEFAULT_LOG_LEVEL;
    private PlannerConfiguration configuration;
    private final Statistics statistics = new Statistics();
    private DefaultParsedProblem parsedProblem;
    private final ErrorManager errorManager = new ErrorManager();

    @Override
    public void setDomain(String domain) {
        this.domainPath = domain;
    }

    @Override
    public String getDomain() {
        return this.domainPath;
    }

    @Override
    public void setProblem(String problem) {
        this.problemPath = problem;
    }

    @Override
    public String getProblem() {
        return this.problemPath;
    }

    @Override
    public void setLogLevel(LogLevel level) {
        this.logLevel = level;
    }

    @Override
    public LogLevel getLogLevel() {
        return this.logLevel;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public int getTimeout() {
        return this.timeout;
    }

    @Override
    public PlannerConfiguration getConfiguration() {
        return this.configuration;
    }

    @Override
    public void setConfiguration(PlannerConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public DefaultParsedProblem parse(String domain, String problem) throws IOException {
        this.setDomain(domain);
        this.setProblem(problem);
        return this.parse();
    }

    @Override
    public DefaultParsedProblem parse() throws IOException {
        // Simulated parsing logic (implement actual parsing if required)
        if (domainPath == null || problemPath == null) {
            throw new FileNotFoundException("Domain or problem path is not set.");
        }
        this.parsedProblem = new DefaultParsedProblem(null); // Replace null with actual Symbol<String> domain
        return this.parsedProblem;
    }

    @Override
    public ErrorManager getParserErrorManager() {
        return this.errorManager;
    }

    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        // Simulated instantiation logic
        return new Problem();
    }

    @Override
    public Plan solve(Problem problem) {
        if (problem == null) {
            throw new IllegalArgumentException("Problem cannot be null.");
        }
        Map<String, Integer> mapping = createVariableMapping(problem);
        List<int[]> cnf = convertPddlToCnf(problem, mapping);
        return solveCnf(cnf, mapping);
    }

    @Override
    public Plan solve() throws InvalidConfigurationException {
        if (this.parsedProblem == null) {
            throw new InvalidConfigurationException("Parsed problem is null.");
        }
        Problem instantiatedProblem = instantiate(this.parsedProblem);
        return solve(instantiatedProblem);
    }

    @Override
    public Statistics getStatistics() {
        return this.statistics;
    }

    @Override
    public boolean hasValidConfiguration() {
        return domainPath != null && problemPath != null;
    }

    @Override
    public boolean isSupported(Problem problem) {
        return problem != null;
    }

    /**
     * Creates a variable mapping for the CNF encoding.
     */
    private Map<String, Integer> createVariableMapping(Problem problem) {
        // Logic to create a mapping between actions and integers for SAT
        return Map.of(); // Placeholder, replace with actual mapping logic
    }

    /**
     * Converts a PDDL problem into CNF.
     */
    private List<int[]> convertPddlToCnf(Problem problem, Map<String, Integer> mapping) {
        List<int[]> cnf = new ArrayList<>();
        problem.getOperators().forEach(op -> {
            int literal = mapping.get(op.getName());
            // Example: Add clauses (simplified, replace with actual CNF generation logic)
            cnf.add(new int[]{literal});
        });
        return cnf;
    }

    /**
     * Solves a CNF problem using SAT4J.
     */
    private Plan solveCnf(List<int[]> cnf, Map<String, Integer> mapping) {
        ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(this.timeout);

        try {
            for (int[] clause : cnf) {
                IVecInt vec = new VecInt(clause);
                solver.addClause(vec);
            }
            if (solver.isSatisfiable()) {
                int[] model = solver.model();
                return buildPlanFromModel(model, mapping);
            }
        } catch (ContradictionException e) {
            System.err.println("Contradiction in CNF.");
        } catch (TimeoutException e) {
            System.err.println("SAT solver timeout.");
        }
        return null;
    }

    /**
     * Converts a SAT model to a PDDL4J Plan.
     */
    private Plan buildPlanFromModel(int[] model, Map<String, Integer> mapping) {
        Plan plan = new Plan();
        for (int literal : model) {
            if (literal > 0) {
                String actionName = mapping.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue().equals(literal))
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse("UnknownAction");
                Operator operator = new Operator();
                operator.setName(actionName);
                plan.add(operator);
            }
        }
        return plan;
    }
}
