package org.jbpm.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kie.internal.definition.KnowledgePackage;
import org.kie.api.definition.process.Process;
import org.kie.api.definition.rule.Global;
import org.kie.api.definition.rule.Query;
import org.kie.api.definition.rule.Rule;
import org.kie.api.definition.type.FactType;

public class ProcessPackage implements KnowledgePackage {

	private String name;
	private List<Process> processes = new ArrayList<Process>();
	
	public ProcessPackage(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}

	public Collection<Rule> getRules() {
		return null;
	}

	public void addProcess(Process process) {
		processes.add(process);
	}
	
	public Collection<Process> getProcesses() {
		return processes;
	}

    public Collection<FactType> getFactTypes() {
        return null;
    }

    public Collection<Query> getQueries() {
        return null;
    }

    public Collection<String> getFunctionNames() {
        return null;
    }

    public Collection<Global> getGlobalVariables() {
        return null;
    }

}
