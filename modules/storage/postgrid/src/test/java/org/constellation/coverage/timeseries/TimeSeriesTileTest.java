/*
 *    Constellation - An open source and standard compliant SDI
 *    http://www.constellation-sdi.org
 *
 *    (C) 2006, Institut de Recherche pour le Développement
 *    (C) 2007 - 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 3 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.constellation.coverage.timeseries;

import junit.framework.*;
import org.geotoolkit.geometry.GeneralEnvelope;
import org.opengis.geometry.Envelope;


/**
 *
 * @author Touraïvane
 */
public class TimeSeriesTileTest extends TestCase {
    
    public TimeSeriesTileTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(TimeSeriesTileTest.class);
    }
    
    /**
     * Test of writeSeries method, of class org.constellation.observation.coverage.TimeSeriesTile.
     */
    public void testWriteSeries() throws Exception {
        
//        for (int i=10; i<=100; i+= 10) {
//            bof(i, i, i);
//        }
//        bof(50, 50, 300);
//        bof(70, 70, 1000);
//        bof(80, 80, 1000);
//        bof(85, 85, 1000);
//        System.exit(0);
        
//        {
//        DummyCoverage2 c = new DummyCoverage2(100, 100, 3);
//        double [] min = {0.0, 0.0, 0.0};
//        double [] max = {100.0, 100.0, 3.0};
//        double [] pas = {1.0, 1.0, 1.0};
//        Envelope e = new GeneralEnvelope(min, max);
//        TimeSeriesTile tsc = new TimeSeriesTile(c, e, 2, pas);
//        tsc.writeSeries(null);
//        tsc.readSeries(0, 1, 1, null);
//
//
//        /*
//        TimeSeriesTile tsc = new TimeSeriesTile(c, e, 2, pas);
//        tsc.writeSeries(null);
//        tsc.readSeries(0, 1, 1, null);
//         **/
//        }
//
//System.exit(0);
        if (true) {
            // TODO: ce test n'est pas au point.
            return;
        }
        DummyCoverage c = new DummyCoverage();
        double [] min = {0.0, 0.0, 0.0};
        double [] max = {9.0, 7.0, 12.0};
        double [] pas = {2.0, 1.0, 1.0};
        Envelope e = new GeneralEnvelope(min, max);
        String s;
        double [] min2 = {100.0, 0.0, 0.0};
        e = new GeneralEnvelope(min2, max);
        s = test(c, e, pas, 2);
        fail();
        
//        double [] min3 = {-2.0, 0.0, 0.0};  //Depuis qu'on a mis les coordonnées réelles, ce n'est plus une faute !
//        try {
//            e = new GeneralEnvelope(min3, max);
//            s = test(c, e, pas, 2);
//            fail();
//        } catch (Exception exp ) { }
        
        double [] min4 = {0.0, 0.0, 0.0, 0.0, 0.0};
        e = new GeneralEnvelope(min4, max);
        s = test(c, e, pas, 2);
        e = new GeneralEnvelope(min, max);
        double [] pas2 = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
        s = test(c, e, pas2, 2);
        double [] pas3 = {5.0, 100.0, 2.0};
        s = test(c, e, pas3, 2);
        s = test(c, e, pas, 5);
        double [] pasx = {2.0, 1.0, 1.0};
        s = test(c, e, pasx, 2);
        assertEquals(
                "84.0 85.0 86.0 87.0 88.0 89.0 90.0 91.0 92.0 93.0 94.0 95.0 \n"+
                "96.0 97.0 98.0 99.0 100.0 101.0 102.0 103.0 104.0 105.0 106.0 107.0 \n"+
                "108.0 109.0 110.0 111.0 112.0 113.0 114.0 115.0 116.0 117.0 118.0 119.0 \n"+
                "120.0 121.0 122.0 123.0 124.0 125.0 126.0 127.0 128.0 129.0 130.0 131.0 \n"+
                "132.0 133.0 134.0 135.0 136.0 137.0 138.0 139.0 140.0 141.0 142.0 143.0 \n"+
                "144.0 145.0 146.0 147.0 148.0 149.0 150.0 151.0 152.0 153.0 154.0 155.0 \n"+
                "156.0 157.0 158.0 159.0 160.0 161.0 162.0 163.0 164.0 165.0 166.0 167.0 \n"+
                "252.0 253.0 254.0 255.0 256.0 257.0 258.0 259.0 260.0 261.0 262.0 263.0 \n"+
                "264.0 265.0 266.0 267.0 268.0 269.0 270.0 271.0 272.0 273.0 274.0 275.0 \n"+
                "276.0 277.0 278.0 279.0 280.0 281.0 282.0 283.0 284.0 285.0 286.0 287.0 \n"+
                "288.0 289.0 290.0 291.0 292.0 293.0 294.0 295.0 296.0 297.0 298.0 299.0 \n"+
                "300.0 301.0 302.0 303.0 304.0 305.0 306.0 307.0 308.0 309.0 310.0 311.0 \n"+
                "312.0 313.0 314.0 315.0 316.0 317.0 318.0 319.0 320.0 321.0 322.0 323.0 \n"+
                "324.0 325.0 326.0 327.0 328.0 329.0 330.0 331.0 332.0 333.0 334.0 335.0 \n"+
                "420.0 421.0 422.0 423.0 424.0 425.0 426.0 427.0 428.0 429.0 430.0 431.0 \n"+
                "432.0 433.0 434.0 435.0 436.0 437.0 438.0 439.0 440.0 441.0 442.0 443.0 \n"+
                "444.0 445.0 446.0 447.0 448.0 449.0 450.0 451.0 452.0 453.0 454.0 455.0 \n"+
                "456.0 457.0 458.0 459.0 460.0 461.0 462.0 463.0 464.0 465.0 466.0 467.0 \n"+
                "468.0 469.0 470.0 471.0 472.0 473.0 474.0 475.0 476.0 477.0 478.0 479.0 \n"+
                "480.0 481.0 482.0 483.0 484.0 485.0 486.0 487.0 488.0 489.0 490.0 491.0 \n"+
                "492.0 493.0 494.0 495.0 496.0 497.0 498.0 499.0 500.0 501.0 502.0 503.0 \n"+
                "588.0 589.0 590.0 591.0 592.0 593.0 594.0 595.0 596.0 597.0 598.0 599.0 \n"+
                "600.0 601.0 602.0 603.0 604.0 605.0 606.0 607.0 608.0 609.0 610.0 611.0 \n"+
                "612.0 613.0 614.0 615.0 616.0 617.0 618.0 619.0 620.0 621.0 622.0 623.0 \n"+
                "624.0 625.0 626.0 627.0 628.0 629.0 630.0 631.0 632.0 633.0 634.0 635.0 \n"+
                "636.0 637.0 638.0 639.0 640.0 641.0 642.0 643.0 644.0 645.0 646.0 647.0 \n"+
                "648.0 649.0 650.0 651.0 652.0 653.0 654.0 655.0 656.0 657.0 658.0 659.0 \n"+
                "660.0 661.0 662.0 663.0 664.0 665.0 666.0 667.0 668.0 669.0 670.0 671.0 \n"+
                "756.0 757.0 758.0 759.0 760.0 761.0 762.0 763.0 764.0 765.0 766.0 767.0 \n"+
                "768.0 769.0 770.0 771.0 772.0 773.0 774.0 775.0 776.0 777.0 778.0 779.0 \n"+
                "780.0 781.0 782.0 783.0 784.0 785.0 786.0 787.0 788.0 789.0 790.0 791.0 \n"+
                "792.0 793.0 794.0 795.0 796.0 797.0 798.0 799.0 800.0 801.0 802.0 803.0 \n"+
                "804.0 805.0 806.0 807.0 808.0 809.0 810.0 811.0 812.0 813.0 814.0 815.0 \n"+
                "816.0 817.0 818.0 819.0 820.0 821.0 822.0 823.0 824.0 825.0 826.0 827.0 \n"+
                "828.0 829.0 830.0 831.0 832.0 833.0 834.0 835.0 836.0 837.0 838.0 839.0 \n",
                s);
        
        s = test2(c, e, pas, 2);
        assertEquals(
"828.0 829.0 830.0 831.0 832.0 833.0 834.0 835.0 836.0 837.0 838.0 839.0 \n"+
"816.0 817.0 818.0 819.0 820.0 821.0 822.0 823.0 824.0 825.0 826.0 827.0 \n"+
"804.0 805.0 806.0 807.0 808.0 809.0 810.0 811.0 812.0 813.0 814.0 815.0 \n"+
"792.0 793.0 794.0 795.0 796.0 797.0 798.0 799.0 800.0 801.0 802.0 803.0 \n"+
"780.0 781.0 782.0 783.0 784.0 785.0 786.0 787.0 788.0 789.0 790.0 791.0 \n"+
"768.0 769.0 770.0 771.0 772.0 773.0 774.0 775.0 776.0 777.0 778.0 779.0 \n"+
"756.0 757.0 758.0 759.0 760.0 761.0 762.0 763.0 764.0 765.0 766.0 767.0 \n"+
"660.0 661.0 662.0 663.0 664.0 665.0 666.0 667.0 668.0 669.0 670.0 671.0 \n"+
"648.0 649.0 650.0 651.0 652.0 653.0 654.0 655.0 656.0 657.0 658.0 659.0 \n"+
"636.0 637.0 638.0 639.0 640.0 641.0 642.0 643.0 644.0 645.0 646.0 647.0 \n"+
"624.0 625.0 626.0 627.0 628.0 629.0 630.0 631.0 632.0 633.0 634.0 635.0 \n"+
"612.0 613.0 614.0 615.0 616.0 617.0 618.0 619.0 620.0 621.0 622.0 623.0 \n"+
"600.0 601.0 602.0 603.0 604.0 605.0 606.0 607.0 608.0 609.0 610.0 611.0 \n"+
"588.0 589.0 590.0 591.0 592.0 593.0 594.0 595.0 596.0 597.0 598.0 599.0 \n"+
"492.0 493.0 494.0 495.0 496.0 497.0 498.0 499.0 500.0 501.0 502.0 503.0 \n"+
"480.0 481.0 482.0 483.0 484.0 485.0 486.0 487.0 488.0 489.0 490.0 491.0 \n"+
"468.0 469.0 470.0 471.0 472.0 473.0 474.0 475.0 476.0 477.0 478.0 479.0 \n"+
"456.0 457.0 458.0 459.0 460.0 461.0 462.0 463.0 464.0 465.0 466.0 467.0 \n"+
"444.0 445.0 446.0 447.0 448.0 449.0 450.0 451.0 452.0 453.0 454.0 455.0 \n"+
"432.0 433.0 434.0 435.0 436.0 437.0 438.0 439.0 440.0 441.0 442.0 443.0 \n"+
"420.0 421.0 422.0 423.0 424.0 425.0 426.0 427.0 428.0 429.0 430.0 431.0 \n"+
"324.0 325.0 326.0 327.0 328.0 329.0 330.0 331.0 332.0 333.0 334.0 335.0 \n"+
"312.0 313.0 314.0 315.0 316.0 317.0 318.0 319.0 320.0 321.0 322.0 323.0 \n"+
"300.0 301.0 302.0 303.0 304.0 305.0 306.0 307.0 308.0 309.0 310.0 311.0 \n"+
"288.0 289.0 290.0 291.0 292.0 293.0 294.0 295.0 296.0 297.0 298.0 299.0 \n"+
"276.0 277.0 278.0 279.0 280.0 281.0 282.0 283.0 284.0 285.0 286.0 287.0 \n"+
"264.0 265.0 266.0 267.0 268.0 269.0 270.0 271.0 272.0 273.0 274.0 275.0 \n"+
"252.0 253.0 254.0 255.0 256.0 257.0 258.0 259.0 260.0 261.0 262.0 263.0 \n"+
"156.0 157.0 158.0 159.0 160.0 161.0 162.0 163.0 164.0 165.0 166.0 167.0 \n"+
"144.0 145.0 146.0 147.0 148.0 149.0 150.0 151.0 152.0 153.0 154.0 155.0 \n"+
"132.0 133.0 134.0 135.0 136.0 137.0 138.0 139.0 140.0 141.0 142.0 143.0 \n"+
"120.0 121.0 122.0 123.0 124.0 125.0 126.0 127.0 128.0 129.0 130.0 131.0 \n"+
"108.0 109.0 110.0 111.0 112.0 113.0 114.0 115.0 116.0 117.0 118.0 119.0 \n"+
"96.0 97.0 98.0 99.0 100.0 101.0 102.0 103.0 104.0 105.0 106.0 107.0 \n"+
"84.0 85.0 86.0 87.0 88.0 89.0 90.0 91.0 92.0 93.0 94.0 95.0 \n", s);         

        s = test3(c, e, pas, 2);

assertEquals(
"828.0 829.0 830.0 831.0 832.0 833.0 834.0 835.0 836.0 837.0 838.0 839.0 \n"+
"816.0 817.0 818.0 819.0 820.0 821.0 822.0 823.0 824.0 825.0 826.0 827.0 \n"+
"804.0 805.0 806.0 807.0 808.0 809.0 810.0 811.0 812.0 813.0 814.0 815.0 \n"+
"792.0 793.0 794.0 795.0 796.0 797.0 798.0 799.0 800.0 801.0 802.0 803.0 \n"+
"780.0 781.0 782.0 783.0 784.0 785.0 786.0 787.0 788.0 789.0 790.0 791.0 \n"+
"768.0 769.0 770.0 771.0 772.0 773.0 774.0 775.0 776.0 777.0 778.0 779.0 \n"+
"756.0 757.0 758.0 759.0 760.0 761.0 762.0 763.0 764.0 765.0 766.0 767.0 \n"+
"660.0 661.0 662.0 663.0 664.0 665.0 666.0 667.0 668.0 669.0 670.0 671.0 \n"+
"648.0 649.0 650.0 651.0 652.0 653.0 654.0 655.0 656.0 657.0 658.0 659.0 \n"+
"636.0 637.0 638.0 639.0 640.0 641.0 642.0 643.0 644.0 645.0 646.0 647.0 \n"+
"624.0 625.0 626.0 627.0 628.0 629.0 630.0 631.0 632.0 633.0 634.0 635.0 \n"+
"612.0 613.0 614.0 615.0 616.0 617.0 618.0 619.0 620.0 621.0 622.0 623.0 \n"+
"600.0 601.0 602.0 603.0 604.0 605.0 606.0 607.0 608.0 609.0 610.0 611.0 \n"+
"588.0 589.0 590.0 591.0 592.0 593.0 594.0 595.0 596.0 597.0 598.0 599.0 \n"+
"492.0 493.0 494.0 495.0 496.0 497.0 498.0 499.0 500.0 501.0 502.0 503.0 \n"+
"480.0 481.0 482.0 483.0 484.0 485.0 486.0 487.0 488.0 489.0 490.0 491.0 \n"+
"468.0 469.0 470.0 471.0 472.0 473.0 474.0 475.0 476.0 477.0 478.0 479.0 \n"+
"456.0 457.0 458.0 459.0 460.0 461.0 462.0 463.0 464.0 465.0 466.0 467.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n"+
"-1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 -1.0 \n", s);
    }
    
    private String test(DummyCoverage c, Envelope e, double [] pas, int vd) throws Exception  {
//        TimeSeriesTile tsc = new TimeSeriesTile(c, e, vd, pas);
//        tsc.writeSeries(null);
//        tsc.debugSeries();
//        TimeSeries [] ts = tsc.getSeries();
//        return tsc.debugSeries();
        return null;
    }
    
    private void test0(DummyCoverage c, Envelope e, double [] pas, int vd) throws Exception  {
//        TimeSeriesTile tsc = new TimeSeriesTile(c, e, vd, pas);
//        tsc.writeSeries(null);
    }
    
    
    private String test2(DummyCoverage c, Envelope e, double [] pas, int vd) throws Exception  {
//        TimeSeriesTile tsc = new TimeSeriesTile(c, e, vd, pas);
//        tsc.writeSeries(null);
//        String s = "";
//        TimeSeries [] ts = tsc.getSeries();
//        for (int i = ts.length-1; i>=0; i--) {
//            double [] d = ts[i].getData(null);
//            for (int j=0; j<d.length; j++) {
//                s += d[j] + " ";
//            }
//            s += "\n";
//        }
//        return s;
        return null;
    }
    private String test3(DummyCoverage c, Envelope e, double [] pas, int vd) throws Exception  {
//        TimeSeriesTile tsc = new TimeSeriesTile(c, e, vd, pas);
//        tsc.writeSeries(null);
//        String s = "";
//        TimeSeries [] ts = tsc.getSeries();
//        double d[] = null;
//        d = ts[0].getData(d);
//        
//        double dd[] = {-1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0};
//        for (int i = 0; i<ts.length/2; i++)
//            ts[i].setData(dd);
//        for (int i = ts.length-1; i>=0; i--) {
//            d = ts[i].getData(d);
//            for (int j=0; j<d.length; j++) {
//                s += d[j] + " ";
//            }
//            s += "\n";
//        }
//        return s;
        return null;
    }
    
    
    
    private void bof(int x, int y, int z)  throws Exception {
//        DummyCoverage c = new DummyCoverage(x, y, z);
//        double [] min = {0.0, 0.0, 0.0};
//        double [] max = {x, y, z};
//        double [] pas = {1.0, 1.0, 1.0};
//        Envelope e = new GeneralEnvelope(min, max);
//        String ss;
//        long tt = System.currentTimeMillis();
//        test0(c, e, pas, 2);
//        System.out.println(System.currentTimeMillis() - tt);
    }
}
