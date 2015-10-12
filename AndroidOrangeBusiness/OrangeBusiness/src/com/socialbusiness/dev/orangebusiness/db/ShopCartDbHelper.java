package com.socialbusiness.dev.orangebusiness.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.socialbusiness.dev.orangebusiness.Constant;
import com.socialbusiness.dev.orangebusiness.manager.SettingsManager;
import com.socialbusiness.dev.orangebusiness.model.Product;
import com.socialbusiness.dev.orangebusiness.model.ShopCartItem;
import com.socialbusiness.dev.orangebusiness.model.User;
import com.socialbusiness.dev.orangebusiness.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ShopCartDbHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "ShopCart.db";
    private static final String TABLE_NAME = "tb_shop_cart";
    private static final String PAYIMAGE_TABLE_NAME = "tb_payimage";
    private Context mContext;

    public ShopCartDbHelper(Context context) {
        super(context.getApplicationContext(), DB_NAME, null, DB_VERSION);
        mContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        doCreateTables(db);
        Log.e("SQLITE", "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.e("SQLITE", "onUpgrade-" + oldVersion + "-" + newVersion);
        doDropTables(db);
        doCreateTables(db);
    }

    private void doCreateTables(SQLiteDatabase db) {
        try {
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "id VARCHAR, "
                    + "name VARCHAR, "
                    + "image VARCHAR, "
                    + "price FLOAT, "
                    + "quantity INTEGER, "
                    + "user_id VARCHAR)");
            db.execSQL("CREATE TABLE " + PAYIMAGE_TABLE_NAME + " ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "payRemark VARCHAR, "
                    + "image0 VARCHAR, "
                    + "image1 VARCHAR, "
                    + "image2 VARCHAR, "
                    + "image3 VARCHAR, "
                    + "image4 VARCHAR, "
                    + "image5 VARCHAR, "
                    + "payMoney VARCHAR, "
                    + "freight VARCHAR, "
                    + "temp0 VARCHAR, "
                    + "temp1 VARCHAR, "
                    + "temp2 VARCHAR, "
                    + "temp3 VARCHAR, "
                    + "user_id VARCHAR)");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDropTables(SQLiteDatabase db) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + PAYIMAGE_TABLE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User getLoginUser() {
        User loginUser = SettingsManager.getLoginUser();
        if (loginUser == null) {
            Intent toLoginIntent = new Intent(Constant.RECEIVER_NO_LOGIN);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(toLoginIntent);
        }
        return loginUser;
    }

    public ShopCartItem addShopCart(Product product) {
        ShopCartItem shopCartItem=null;
        if (product != null) {
            User loginUser = getLoginUser();
            if (loginUser == null) {
                return shopCartItem;
            }
            shopCartItem= getShopCartItemByProductId(product.id);
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            if (shopCartItem != null) {
                //数据库里已经有此产品，则数量+1
                values.put("quantity", shopCartItem.quantity + 1);
                db.update(TABLE_NAME, values, "id = ? AND user_id = ?", new String[]{product.id, loginUser.id});
            } else {
                //未添加此产品，新增一条记录
                values.put("id", product.id);
                values.put("name", product.name);
                values.put("image", product.getCoverImage());
                values.put("price", product.price);
                values.put("quantity", 1);
                values.put("user_id", loginUser.id);
                db.insert(TABLE_NAME, null, values);
            }

            db.close();
        }

        return shopCartItem;
    }

    public void addShopCart(ShopCartItem shopCartItem) {
        if (shopCartItem != null) {
            User loginUser = getLoginUser();
            if (loginUser == null) {
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("quantity", shopCartItem.quantity + 1);
            db.update(TABLE_NAME, values, "id = ? AND user_id = ?", new String[]{shopCartItem.id, loginUser.id});

            db.close();
        }
    }

    public void minusShopCart(ShopCartItem shopCartItem) {
        if (shopCartItem != null) {
            User loginUser = getLoginUser();
            if (loginUser == null) {
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("quantity", shopCartItem.quantity - 1);
            db.update(TABLE_NAME, values, "id = ? AND user_id = ?", new String[]{shopCartItem.id, loginUser.id});

            db.close();
        }
    }

    public void updateShopCartPrice(ShopCartItem shopCartItem) {
        if (shopCartItem != null) {
            User loginUser = getLoginUser();
            if (loginUser == null) {
                return;
            }
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("price", shopCartItem.price);
            db.update(TABLE_NAME, values, "id = ? AND user_id = ?", new String[]{shopCartItem.id, loginUser.id});

            db.close();
        }
    }

    public ShopCartItem getShopCartItemByProductId(String productId) {
        if (TextUtils.isEmpty(productId)) {
            return null;
        }

        User loginUser = getLoginUser();
        if (loginUser == null) {
            return null;
        }

        ShopCartItem shopCartItem = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "id = ? AND user_id = ?", new String[]{productId, loginUser.id}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToNext()) {
                shopCartItem = new ShopCartItem();
                shopCartItem.id = cursor.getString(cursor.getColumnIndex("id"));
                shopCartItem.name = cursor.getString(cursor.getColumnIndex("name"));
                shopCartItem.image = cursor.getString(cursor.getColumnIndex("image"));
                shopCartItem.price = cursor.getFloat(cursor.getColumnIndex("price"));
                shopCartItem.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
            }
            cursor.close();
        }
        db.close();
        return shopCartItem;
    }

    SQLiteDatabase dbNoClose;

    public void closeOpened()

    {

        if (dbNoClose != null) {
            dbNoClose.close();
        }
    }

//    public ShopCartItem getShopCartItemByProductIdNoClose(String productId) {
//        if (TextUtils.isEmpty(productId)) {
//            return null;
//        }
//
//        User loginUser = getLoginUser();
//        if (loginUser == null) {
//            return null;
//        }
//
//        ShopCartItem shopCartItem = null;
//        if (dbNoClose == null)
//            dbNoClose = getReadableDatabase();
//        Cursor cursor = dbNoClose.query(TABLE_NAME, null, "id = ? AND user_id = ?", new String[]{productId, loginUser.id}, null, null, null);
//        if (cursor != null) {
//            if (cursor.moveToNext()) {
//                shopCartItem = new ShopCartItem();
//                shopCartItem.id = cursor.getString(cursor.getColumnIndex("id"));
//                shopCartItem.name = cursor.getString(cursor.getColumnIndex("name"));
//                shopCartItem.image = cursor.getString(cursor.getColumnIndex("image"));
//                shopCartItem.price = cursor.getFloat(cursor.getColumnIndex("price"));
//                shopCartItem.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
//            }
//            cursor.close();
//        }
////        db.close();
//        return shopCartItem;
//    }

    public List<ShopCartItem> getAllShopCartItems() {
        List<ShopCartItem> items = new ArrayList<>();
        User loginUser = getLoginUser();
//        Log.e("===","=="+loginUser.id);
        if (loginUser == null) {
            return items;
        }

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "user_id = ?", new String[]{loginUser.id}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ShopCartItem shopCartItem = new ShopCartItem();
                shopCartItem.id = cursor.getString(cursor.getColumnIndex("id"));
                shopCartItem.name = cursor.getString(cursor.getColumnIndex("name"));
                shopCartItem.image = cursor.getString(cursor.getColumnIndex("image"));
                shopCartItem.price = cursor.getFloat(cursor.getColumnIndex("price"));
                shopCartItem.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                items.add(shopCartItem);
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    public int getAllShopCartItemsCount() {
        List<ShopCartItem> items = new ArrayList<>();
        User loginUser = getLoginUser();
//        Log.e("===","=="+loginUser.id);
        if (loginUser == null) {
            return 0;
        }
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "user_id = ?", new String[]{loginUser.id}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {

                count+=cursor.getInt(cursor.getColumnIndex("quantity"));
            }
            cursor.close();
        }
        db.close();
        return count;
    }

    public List<ShopCartItem> getShopCartItemsByUserId(String userId) {
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        List<ShopCartItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, null, "user_id = ?", new String[]{userId}, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ShopCartItem shopCartItem = new ShopCartItem();
                shopCartItem.id = cursor.getString(cursor.getColumnIndex("id"));
                shopCartItem.name = cursor.getString(cursor.getColumnIndex("name"));
                shopCartItem.image = cursor.getString(cursor.getColumnIndex("image"));
                shopCartItem.price = cursor.getFloat(cursor.getColumnIndex("price"));
                shopCartItem.quantity = cursor.getInt(cursor.getColumnIndex("quantity"));
                items.add(shopCartItem);
            }
            cursor.close();
        }
        db.close();
        return items;
    }

    public void resetNum(String shopCartId, int num) {
        if (TextUtils.isEmpty(shopCartId)) {
            return;
        }
        User loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }
        if (num <= 0) {
            num = 0;
        }
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("quantity", num);
        db.update(TABLE_NAME, values, "id = ? AND user_id = ?", new String[]{shopCartId, loginUser.id});
        db.close();
    }

    public void deleteShopCardItems(List<ShopCartItem> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        User loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }
        StringBuffer inString = new StringBuffer();
        for (int i = 0; i < items.size(); i++) {
            ShopCartItem item = items.get(i);
            if (i != 0) {
                inString.append(",");
            }
            inString.append("'");
            inString.append(item.id);
            inString.append("'");
        }

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME +
                " WHERE user_id = '" + loginUser.id + "' AND id IN (" + inString.toString() + ")");
        db.close();
    }

    public boolean deleteShopCardItemById(String id) {
        if (TextUtils.isEmpty(id)) {
            return false;
        }
        User loginUser = getLoginUser();
        if (loginUser == null) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NAME +
                " WHERE user_id = '" + loginUser.id + "' AND id = '" + id + "'");
        db.close();
        return true;
    }

    public void clearShopCart() {
        User loginUser = getLoginUser();
        if (loginUser == null) {
            return;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, "user_id = ?", new String[]{loginUser.id});
        db.close();
    }

}
