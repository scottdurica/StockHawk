package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by scott on 9/8/2016.
 */
public class HDColumns {
    @DataType(DataType.Type.INTEGER) @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String SYMBOL = "symbol";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String PERCENT_CHANGE = "percent_change";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String CHANGE = "change";
    @DataType(DataType.Type.TEXT) @NotNull
    public static final String BIDPRICE = "bid_price";
    @DataType(DataType.Type.TEXT)
    public static final String CREATED = "created";
    @DataType(DataType.Type.INTEGER) @NotNull
    public static final String ISUP = "is_up";
    @DataType(DataType.Type.INTEGER) @NotNull
    public static final String ISCURRENT = "is_current";

    public static final String []STOCK_COLUMNS = {
            QuoteColumns._ID,
            QuoteColumns.BIDPRICE,
            QuoteColumns.SYMBOL,
            QuoteColumns.CHANGE,
            QuoteColumns.CREATED,
            QuoteColumns.PERCENT_CHANGE,
            QuoteColumns.ISUP,
            QuoteColumns.ISCURRENT
    };
    public static final int INDEX_ID = 0;
    public static final int INDEX_BID_PRICE = 1;
    public static final int INDEX_SYMBOL = 2;
    public static final int INDEX_CHANGE = 3;
    public static final int INDEX_CREATED = 4;
    public static final int INDEX_PERCENT_CHANGE = 5;
    public static final int INDEX_IS_UP = 6;
    public static final int INDEX_IS_CURRENT = 7;
}
