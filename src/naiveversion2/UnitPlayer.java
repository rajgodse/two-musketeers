package naiveversion2;

import aic2021.user.UnitController;
import aic2021.user.UnitType;
import naiveversion2.Axeman;
import naiveversion2.Barracks;
import naiveversion2.Base;
import naiveversion2.Explorer;
import naiveversion2.Farm;
import naiveversion2.MyUnit;
import naiveversion2.Quarry;
import naiveversion2.Sawmill;
import naiveversion2.Settlement;
import naiveversion2.Spearman;
import naiveversion2.Trapper;
import naiveversion2.Wolf;
import naiveversion2.Worker;

public class UnitPlayer {

	UnitController uc;
	MyUnit u;

	public void run(UnitController uc) {
		this.uc = uc;
		init();

		while (true) {
			try {
				u.playRound();
			} catch(Exception e) {
				init();
				uc.println(e);
			}

			uc.yield();
		}
	}

	private void init() {
		UnitType t = uc.getType();

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
	}
}
