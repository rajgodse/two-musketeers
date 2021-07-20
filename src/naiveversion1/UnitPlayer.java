package naiveversion1;

import aic2021.user.UnitController;
import aic2021.user.UnitType;
import naiveversion1.Axeman;
import naiveversion1.Barracks;
import naiveversion1.Base;
import naiveversion1.Explorer;
import naiveversion1.Farm;
import naiveversion1.MyUnit;
import naiveversion1.Quarry;
import naiveversion1.Sawmill;
import naiveversion1.Settlement;
import naiveversion1.Spearman;
import naiveversion1.Trapper;
import naiveversion1.Wolf;
import naiveversion1.Worker;

public class UnitPlayer {

	public void run(UnitController uc) {

		UnitType t = uc.getType();
		MyUnit u;

		if (t == UnitType.BASE) u = new Base(uc);
		else if (t == UnitType.WORKER) u = new Worker(uc);
		else if (t == UnitType.EXPLORER) u = new Explorer(uc);
		else if (t == UnitType.TRAPPER) u = new Trapper(uc);
		else if (t == UnitType.AXEMAN) u = new Axeman(uc);
		else if (t == UnitType.SPEARMAN) u = new Spearman(uc);
		else if (t == UnitType.WOLF) u = new Wolf(uc);
		else if (t == UnitType.SETTLEMENT) u = new Settlement(uc);
		else if (t == UnitType.BARRACKS) u = new Barracks(uc);
		else if (t == UnitType.FARM) u = new Farm(uc);
		else if (t == UnitType.QUARRY) u = new Quarry(uc);
		else u = new Sawmill(uc);

		while (true) {
			u.playRound();
			uc.yield();
		}
	}

}
