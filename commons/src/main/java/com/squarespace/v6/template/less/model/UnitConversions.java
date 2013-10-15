package com.squarespace.v6.template.less.model;

import static com.squarespace.v6.template.less.model.Unit.CM;
import static com.squarespace.v6.template.less.model.Unit.DEG;
import static com.squarespace.v6.template.less.model.Unit.DPCM;
import static com.squarespace.v6.template.less.model.Unit.DPI;
import static com.squarespace.v6.template.less.model.Unit.DPPX;
import static com.squarespace.v6.template.less.model.Unit.GRAD;
import static com.squarespace.v6.template.less.model.Unit.HZ;
import static com.squarespace.v6.template.less.model.Unit.IN;
import static com.squarespace.v6.template.less.model.Unit.KHZ;
import static com.squarespace.v6.template.less.model.Unit.MM;
import static com.squarespace.v6.template.less.model.Unit.MS;
import static com.squarespace.v6.template.less.model.Unit.PC;
import static com.squarespace.v6.template.less.model.Unit.PT;
import static com.squarespace.v6.template.less.model.Unit.PX;
import static com.squarespace.v6.template.less.model.Unit.RAD;
import static com.squarespace.v6.template.less.model.Unit.S;
import static com.squarespace.v6.template.less.model.Unit.TURN;



/**
 * Create a 2-way conversion mapping between different Units.
 */
public class UnitConversions {

  private static final double[][] CONVERSIONS;

  private static void create(Unit from, Unit to, double factor) {
    int i0 = from.ordinal();
    int i1 = to.ordinal();
    CONVERSIONS[i0][i1] = factor;
    CONVERSIONS[i1][i0] = 1.0 / factor;
  }
  
  static {
    Unit[] values = Unit.values();
    int sz = values.length;
    CONVERSIONS = new double[sz][sz];
    for (Unit unit : values) {
      create(unit, unit, 1.0);
    }
    
    create(IN, CM, 2.54);
    create(IN, MM, 2.54 * 1000.0);
    create(IN, PX, 96.0);
    create(IN, PT, 72.0);
    create(IN, PC, 12.0 * 72.0);

    create(CM, MM, 1000.0);
    create(CM, PX, 2.54 * 96.0);
    create(CM, PT, 2.54 * 72.0);
    create(CM, PC, 2.54 * 72.0 * 12.0);

    create(PX, MM, (2.54 * 1000.0) / 96.0);
    create(PX, PT, 0.75);
    create(PX, PC, 0.75 / 12.0);

    create(PC, MM, 1000.0 * factor(PC, CM));
    create(PC, PT, 12.0);

    create(PT, MM, (2.54 * 1000.0) / 72.0);
    
    create(S, MS, 1000.0);

    create(KHZ, HZ, 1000.0);
    
    create(DPCM, DPI, 2.54);
    create(DPPX, DPI, 96.0);
    create(DPPX, DPCM, 2.54 * 96.0);

    create(TURN, DEG, 360.0);
    create(TURN, GRAD, 400.0);
    create(TURN, RAD, 2 * Math.PI);
    create(DEG, RAD, 1.0 / (180.0 / Math.PI));
    create(DEG, GRAD, 9 / 10.0);
    create(RAD, GRAD, 1 / (Math.PI / 200.0));
  }
  
  /**
   * Obtain the multiplication factor to convert between units. For example, factor(IN, PX)
   * means "convert from inches to pixels" and would return 96.
   * 
   * Incompatible conversions return 0.0, letting the caller throw an appropriate error.
   * Upstream less.js doesn't perform conversion and will drop the units from the
   * right-hand side of the operation.
   * 
   * For example, in upstream less.js:
   * 
   *   font-size: 12px + 3in - 4s + 24%;
   * 
   * Evalutes to:
   * 
   *   font-size: 35px;
   *   
   * ... which seems nuts.
   * 
   * There are also cases in CSS3 where expressions need to be kept intact and
   * not collapsed, since they need to be evaluated by the browser. The calc()
   * function, for example:
   *  http://www.w3.org/TR/css3-values/#calc
   * 
   */
  public static double factor(Unit from, Unit to) {
    if (from == null || to == null) {
      return 1.0;
    }
    return CONVERSIONS[from.ordinal()][to.ordinal()];
  }

}
