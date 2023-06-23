package at.ac.tuwien.ifs.sge.agent.macroactions;

import at.ac.tuwien.ifs.sge.agent.Command;
import at.ac.tuwien.ifs.sge.agent.ExecutableActionFactoryException;

import java.util.Deque;
import java.util.Map;
import java.util.UUID;

public interface MacroAction<EmpireEvent>{

    Deque<MacroAction<EmpireEvent>> generateExecutableAction(Map<UUID,Deque<Command<EmpireEvent>>> unitsCommandQueues) throws ExecutableActionFactoryException;

    Deque<EmpireEvent> getResponsibleActions(Map<UUID,Deque<Command<EmpireEvent>>> unitCommandQueues) throws ExecutableActionFactoryException;

}
