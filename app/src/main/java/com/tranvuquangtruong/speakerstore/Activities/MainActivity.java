package com.tranvuquangtruong.speakerstore.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Adapters.ProductAdapter;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GridView gridView;
    private DBHelper dbHelper;
    private List<ProductModel> productList;
    private ProductAdapter productAdapter;
    private ActivityResultLauncher<Intent> addProductLauncher;
    private  ActivityResultLauncher<Intent> deleteProductLauncher;
    private static boolean isNewActivityOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = findViewById(R.id.gridView);
        dbHelper = new DBHelper(this);
        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(this, productList);
        gridView.setAdapter(productAdapter);

        Button btnReload = findViewById(R.id.btnRefresh);
        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Gọi phương thức để load lại trang
                refreshGridViewEmpty();
                loadProductsFromDatabase();
                TextView textView = findViewById(R.id.tvBrandMain);
                textView.setText("Sắp xếp sản phẩm");//refresh giá trị textview
            }
        });
        

        // Load dữ liệu từ SQLite và cập nhật GridView
        loadProductsFromDatabase();

        LinearLayout menuSortPrice = findViewById(R.id.layoutMenuSortPriceMain);
        TextView tvSortProduct = findViewById(R.id.tvBrandMain);
        menuSortPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this,menuSortPrice);
                popupMenu.getMenuInflater().inflate(R.menu.sort_price_menu,popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_tangdan){
                            refreshGridViewEmpty();
                            loadProductsSortedASCByPrice();
                            tvSortProduct.setText("Tăng dần");
                        }
                        else if (item.getItemId() == R.id.menu_giamdan){
                            refreshGridViewEmpty();
                            loadProductsSortedDECByPrice();
                            tvSortProduct.setText("Giảm dần");
                        }
                        else if (item.getItemId() == R.id.menu_JBL) {
                            refreshGridViewEmpty();
                            String[] dieuKien = {"JBL"};
                            loadProductsSortedBrand(dieuKien);
                            tvSortProduct.setText("JBL");
                        }
                        else if (item.getItemId() == R.id.menu_sony) {
                            refreshGridViewEmpty();
                            String[] dieuKien = {"Sony"};
                            loadProductsSortedBrand(dieuKien);
                            tvSortProduct.setText("Sony");
                        }
                        else if (item.getItemId() == R.id.menu_samsung) {
                            refreshGridViewEmpty();
                            String[] dieuKien = {"SamSung"};
                            loadProductsSortedBrand(dieuKien);
                            tvSortProduct.setText("SamSung");
                        }
                        return false;
                    }
                });
                popupMenu.show();
        }
        });

        // Kiểm tra trạng thái của trang mới được mở
        if (isNewActivityOpened) {
            // Trang mới được mở
        } else {
            // Trang mới chưa được mở
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result if needed
                    }
                }
        );
        deleteProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result if needed
                    }
                }
        );
        gridView.setAdapter(productAdapter);
        gridView.setOnItemClickListener(((parent, view, position, id) -> {
            ProductModel selectProductModel = productList.get(position);
            openDetailProductActivity(selectProductModel.getId());
        }));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        new MenuInflater(this).inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Xử lý sự kiện khi một mục menu được chọn
        if (item.getItemId()==R.id.menu_add){
            isNewActivityOpened = true;
            // Open AddProductActivity using the new launcher
            Intent intent = new Intent(MainActivity.this, AddProductActivity.class);
            addProductLauncher.launch(intent);
        }
        else if (item.getItemId() == R.id.menu_delete) {
            isNewActivityOpened = true;
            Intent intent = new Intent(MainActivity.this , DeleteProductActivity.class);
            deleteProductLauncher.launch(intent);
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadProductsFromDatabase( ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DBHelper.COLUMN_ID, DBHelper.COLUMN_NAME, DBHelper.COLUMN_PRICE, DBHelper.COLUMN_QUANTITY, DBHelper.COLUMN_IMAGE,DBHelper.COLUMN_BRAND};
        Cursor cursor = db.query(DBHelper.TABLE_PRODUCTS, columns, null, null, null, null, null);

        if (cursor != null) {
            int columnIndexId = cursor.getColumnIndex(DBHelper.COLUMN_ID);
            int columnIndexName = cursor.getColumnIndex(DBHelper.COLUMN_NAME);
            int columnIndexPrice = cursor.getColumnIndex(DBHelper.COLUMN_PRICE);
            int columnIndexQuantity = cursor.getColumnIndex(DBHelper.COLUMN_QUANTITY);
            int columnIndexImage = cursor.getColumnIndex(DBHelper.COLUMN_IMAGE);
            int columnIndexBrand = cursor.getColumnIndex(DBHelper.COLUMN_BRAND);

            if (cursor.moveToFirst()) {
                do {
                    // Check if columnIndex is valid (not -1) before using it
                    if (columnIndexId != -1 && columnIndexName != -1 && columnIndexPrice != -1
                            && columnIndexQuantity != -1 && columnIndexImage != -1 && columnIndexBrand != -1) {

                        int id = cursor.getInt(columnIndexId);
                        String name = cursor.getString(columnIndexName);
                        double price = cursor.getDouble(columnIndexPrice);
                        int quantity = cursor.getInt(columnIndexQuantity);
                        byte[] image = cursor.getBlob(columnIndexImage);
                        String brand = cursor.getString(columnIndexBrand);

                        ProductModel product = new ProductModel(id,name, price, quantity, image,brand);
                        productList.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            // Cập nhật Adapter khi có dữ liệu mới
            productAdapter.notifyDataSetChanged();
        }
    }
    private void loadProductsSortedASCByPrice() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Sắp xếp theo giá tăng dần
        String query = "SELECT * FROM " + DBHelper.TABLE_PRODUCTS + " ORDER BY " + DBHelper.COLUMN_PRICE + " ASC";
        Cursor cursor = db.rawQuery(query, null);
        int idIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME);
        int priceIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PRICE);
        int quantityIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_QUANTITY);
        int imageIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGE);
        int brandIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BRAND);
        if (cursor.moveToFirst()) {
            do {
                // Check if columnIndex is valid (not -1) before using it
                if (idIndex != -1 && nameIndex != -1 && priceIndex != -1
                        && quantityIndex != -1 && imageIndex != -1 && brandIndex != -1) {

                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    double price = cursor.getDouble(priceIndex);
                    int quantity = cursor.getInt(quantityIndex);
                    byte[] image = cursor.getBlob(imageIndex);
                    String brand = cursor.getString(brandIndex);

                    ProductModel product = new ProductModel(id,name, price, quantity, image, brand);
                    productList.add(product);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Cập nhật Adapter khi có dữ liệu mới
        productAdapter.notifyDataSetChanged();
    }
    private void loadProductsSortedDECByPrice( ) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Sắp xếp theo giá giảm dần
        String query = "SELECT * FROM " + DBHelper.TABLE_PRODUCTS + " ORDER BY " + DBHelper.COLUMN_PRICE + " DESC";
        Cursor cursor = db.rawQuery(query, null);
        int idIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME);
        int priceIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PRICE);
        int quantityIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_QUANTITY);
        int imageIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGE);
        int brandIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BRAND);
        if (cursor.moveToFirst()) {
            do {
                // Check if columnIndex is valid (not -1) before using it
                if (idIndex != -1 && nameIndex != -1 && priceIndex != -1
                        && quantityIndex != -1 && imageIndex != -1 && brandIndex != -1) {

                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    double price = cursor.getDouble(priceIndex);
                    int quantity = cursor.getInt(quantityIndex);
                    byte[] image = cursor.getBlob(imageIndex);
                    String brand = cursor.getString(brandIndex);

                    ProductModel product = new ProductModel(id,name, price, quantity, image, brand);
                    productList.add(product);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Cập nhật Adapter khi có dữ liệu mới
        productAdapter.notifyDataSetChanged();
    }

    private void loadProductsSortedBrand(String[] dieuKien) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        // Sắp xếp theo giá tăng dần
        String query = "SELECT * FROM " + DBHelper.TABLE_PRODUCTS + " WHERE " + DBHelper.COLUMN_BRAND + "= ?";
        String[] selectionArgs = dieuKien;
        Cursor cursor = db.rawQuery(query, selectionArgs);
        int idIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_ID);
        int nameIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_NAME);
        int priceIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_PRICE);
        int quantityIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_QUANTITY);
        int imageIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_IMAGE);
        int brandIndex = cursor.getColumnIndexOrThrow(DBHelper.COLUMN_BRAND);
        if (cursor.moveToFirst()) {
            do {
                // Check if columnIndex is valid (not -1) before using it
                if (idIndex != -1 && nameIndex != -1 && priceIndex != -1
                        && quantityIndex != -1 && imageIndex != -1 && brandIndex != -1) {

                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    double price = cursor.getDouble(priceIndex);
                    int quantity = cursor.getInt(quantityIndex);
                    byte[] image = cursor.getBlob(imageIndex);
                    String brand = cursor.getString(brandIndex);

                    ProductModel product = new ProductModel(id,name, price, quantity, image, brand);
                    productList.add(product);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Cập nhật Adapter khi có dữ liệu mới
        productAdapter.notifyDataSetChanged();
    }
    private void refreshGridViewEmpty() {
        // Cập nhật adapter với dữ liệu mới (trống)
        productAdapter.clearData();
    }

    private void openDetailProductActivity(int productID) {
        // Mở trang EditProductActivity và chuyển ID sản phẩm cần chỉnh sửa
        Intent intent = new Intent(this, ShowProductDetailActivity.class);
        intent.putExtra("productId", productID);
        startActivity(intent);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database when the activity is destroyed
        dbHelper.close();
    }

}