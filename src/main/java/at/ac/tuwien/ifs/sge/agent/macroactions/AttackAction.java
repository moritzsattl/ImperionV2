package at.ac.tuwien.ifs.sge.agent.macroactions;

import at.ac.tuwien.ifs.sge.agent.Command;
import at.ac.tuwien.ifs.sge.agent.ExecutableActionFactoryException;
import at.ac.tuwien.ifs.sge.agent.GameStateNode;
import at.ac.tuwien.ifs.sge.agent.Imperion;
import at.ac.tuwien.ifs.sge.agent.astar.AStar;
import at.ac.tuwien.ifs.sge.agent.astar.AStarNode;
import at.ac.tuwien.ifs.sge.core.engine.logging.Logger;
import at.ac.tuwien.ifs.sge.game.empire.communication.event.order.start.MovementStartOrder;
import at.ac.tuwien.ifs.sge.game.empire.exception.EmpireMapException;
import at.ac.tuwien.ifs.sge.game.empire.map.Position;
import at.ac.tuwien.ifs.sge.game.empire.model.units.EmpireUnit;

import java.util.*;


public class AttackAction<EmpireEvent> extends AttackMacroAction<EmpireEvent>{

    private final EmpireUnit unit;
    private final EmpireUnit enemyUnit;

    private Position attackingFrom;

    private Deque<EmpireEvent> path;

    private final MacroActionType macroType;

    private final boolean force;

    public AttackAction(GameStateNode<EmpireEvent> gameStateNode, int playerId ,MacroActionType action, Logger log, boolean simulation, EmpireUnit unit, EmpireUnit enemyUnit, boolean force) {
        super(gameStateNode, playerId, log, simulation);
        this.unit = unit;
        this.macroType = action;
        this.enemyUnit = enemyUnit;
        this.force = force;
        this.path = null;
    }

    public boolean isForce() {
        return force;
    }

    public EmpireUnit getUnit() {
        return unit;
    }
    public UUID getUnitId() {
        return unit.getId();
    }
    public boolean isWon() {
        return unit.isAlive() && !enemyUnit.isAlive();
    }
    public boolean unitDied() { return !unit.isAlive(); }

    @Override
    public Deque<EmpireEvent> getResponsibleActions(Map<UUID, Deque<Command<EmpireEvent>>> unitCommandQueues) throws ExecutableActionFactoryException {
        double currentShortestDist = Double.MAX_VALUE;
        //log.info("Attack Action, get all neighbour positions of enemy " + enemyUnit);
        for (var pos: enemyUnit.getPosition().getAllNeighbours()) {
            try {
                var tile = game.getBoard().getTile(pos.getX(),pos.getY());
                if((tile != null && tile.getOccupants() != null && game.getBoard().isMovementPossible(pos.getX(),pos.getY(),playerId))){
                    double dist = Imperion.getEuclideanDistance(unit.getPosition(),pos);
                    //log.info("From unit to tile distance : " + dist);
                    if(dist < currentShortestDist){
                        currentShortestDist = dist;
                        attackingFrom = pos;
                    }

                }
            } catch (EmpireMapException e) {
                if(!simulation){
                    log.info(e.getClass());
                    log.info(e);
                }
            }
        }


        if(attackingFrom == null){
            throw new ExecutableActionFactoryException("No tile to attack from");
        }

        log.debug("Attack from pos" + attackingFrom);

        if(path == null) {
            AStar aStar = new AStar(unit.getPosition(),attackingFrom,gameStateNode,playerId, log);
            //log.info("Calculated Path: ");
            AStarNode currentNode = aStar.findPath();
            if(currentNode == null) return null;

            path = new ArrayDeque<>();

            while (currentNode != null) {
                path.addFirst((EmpireEvent) new MovementStartOrder(unit.getId(),currentNode.getPosition()));

                // Next Position to move to
                currentNode = currentNode.getPrev();
            }


        }
        path.poll();

        if(path.isEmpty()){
            throw new ExecutableActionFactoryException("Path to " + attackingFrom + " was not found by unit " + unit);
        }

        return path;
    }

    @Override
    public String toString() {
        return "AttackAction{" +
                "unit=" + unit +
                ", enemyUnit=" + enemyUnit +
                ", attackingFrom=" + attackingFrom +
                ", macroType=" + macroType +
                '}';
    }

    public MacroActionType getMacroType() {
        return macroType;
    }

    public EmpireUnit getEnemyUnit() {
        return enemyUnit;
    }
}
