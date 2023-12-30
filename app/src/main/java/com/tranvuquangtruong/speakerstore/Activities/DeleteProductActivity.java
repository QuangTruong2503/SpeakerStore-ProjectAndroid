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
import android.os.Handler;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.tranvuquangtruong.speakerstore.DBHelper;
import com.tranvuquangtruong.speakerstore.Adapters.ProductListViewAdapter;
import com.tranvuquangtruong.speakerstore.Models.ProductModel;
import com.tranvuquangtruong.speakerstore.R;

import java.util.ArrayList;
import java.util.List;

public class DeleteProductActivity extends AppCompatActivity {

    private static final int INTERVAL_MILLIS = 5000; // Thời gian giữa các lần lặp, 5000 milliseconds = 5 seconds
    private Handler handler;
    private GridView gridView;
    private Button btnDeleteProduct;
    private List<ProductModel> productModelList;
    private DBHelper dbHelper;
    private ProductListViewAdapter productAdapter;
    private ActivityResultLauncher<Intent> editProductLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_product);

        gridView = findViewById(R.id.gvDeleteProduct);
        dbHelper = new DBHelper(this);
        productModelList = new ArrayList<>();
        productAdapter = new ProductListViewAdapter(this, productModelList);
        gridView.setAdapter(productAdapter);
        // Load dữ liệu từ SQLite và cập nhật ListView
        loadProductsFromDatabase();

        LinearLayout menuSortPrice = findViewById(R.id.layoutMenuSortPriceEdit);

        menuSortPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(DeleteProductActivity.this,menuSortPrice);
                popupMenu.getMenuInflater().inflate(R.menu.sort_price_menu,popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.menu_tangdan){
                            productAdapter.clearData();
                            loadProductsSortedASCByPrice();
                        }
                        else if (item.getItemId() == R.id.menu_giamdan){
                            productAdapter.clearData();
                            loadProductsSortedDECByPrice();
                        }
                        else if (item.getItemId() == R.id.menu_JBL) {
                            productAdapter.clearData();
                            String[] dieuKien = {"JBL"};
                            loadProductsSortedBrand(dieuKien);
                        }
                        else if (item.getItemId() == R.id.menu_sony) {
                            productAdapter.clearData();
                            String[] dieuKien = {"Sony"};
                            loadProductsSortedBrand(dieuKien);
                        }
                        else if (item.getItemId() == R.id.menu_samsung) {
                            productAdapter.clearData();
                            String[] dieuKien = {"SamSung"};
                            loadProductsSortedBrand(dieuKien);
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });
        // Thiết lập sự kiện khi nhấn vào nút Xóa
        productAdapter.setOnDeleteClickListener(position -> {
            // Xử lý xóa sản phẩm từ cơ sở dữ liệu
            dbHelper = new DBHelper(DeleteProductActivity.this);
            boolean isDeleted = dbHelper.deleteProduct(productModelList.get(position).getId());

            if (isDeleted) {
                // Xóa sản phẩm thành công, cập nhật danh sách và thông báo
                productModelList.remove(position);
                productAdapter.notifyDataSetChanged();
                Toast.makeText(DeleteProductActivity.this, "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
            } else {
                // Xóa sản phẩm không thành công, thông báo lỗi
                Toast.makeText(DeleteProductActivity.this, "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        productAdapter.setOnEditClickListener((position, view) -> {
            // Lấy sản phẩm được chọn
             ProductModel selectedProduct = (ProductModel) productAdapter.getItem(position);
            // Mở trang EditProductActivity để chỉnh sửa thông tin sản phẩm
            openEditProductActivity(selectedProduct.getId());

        });

        editProductLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Handle the result if needed
                    }
                }
        );
        Button btnRefresh = findViewById(R.id.btnRefreshDelete);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                productAdapter.clearData();
                loadProductsFromDatabase();
            }
        });
        // Set up the Toolbar (ActionBar) with a back button
        Toolbar toolbar = findViewById(R.id.toolbarDelete);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    // Handle the back button in the ActionBar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle the back button click
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        if (item.getItemId() == R.id.menu_tangdan){
            productAdapter.clearData();
            loadProductsSortedASCByPrice();
        }
        else if (item.getItemId() == R.id.menu_giamdan){
            productAdapter.clearData();
            loadProductsSortedDECByPrice();
        }
        return super.onContextItemSelected(item);
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Tạo menu context
        getMenuInflater().inflate(R.menu.sort_price_menu, menu);
    }

    private void loadProductsFromDatabase() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] columns = {DBHelper.COLUMN_ID, DBHelper.COLUMN_NAME, DBHelper.COLUMN_PRICE,
                DBHelper.COLUMN_QUANTITY, DBHelper.COLUMN_IMAGE, DBHelper.COLUMN_BRAND};
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

                        ProductModel product = new ProductModel(id,name, price, quantity, image, brand);
                        productModelList.add(product);
                    }
                } while (cursor.moveToNext());
            }
            cursor.close();
            db.close();

            // Cập nhật Adapter khi có dữ liệu mới
            productAdapter.notifyDataSetChanged();
        }
    }
    private void loadProductsSortedASCByPrice( ) {
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
                    productModelList.add(product);
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
        // Sắp xếp theo giá tăng dần
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
                        && quantityIndex != -1 && imageIndex != -1) {

                    int id = cursor.getInt(idIndex);
                    String name = cursor.getString(nameIndex);
                    double price = cursor.getDouble(priceIndex);
                    int quantity = cursor.getInt(quantityIndex);
                    byte[] image = cursor.getBlob(imageIndex);
                    String brand = cursor.getString(brandIndex);
                    ProductModel product = new ProductModel(id,name, price, quantity, image, brand);
                    productModelList.add(product);
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
                    productModelList.add(product);
                }
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        // Cập nhật Adapter khi có dữ liệu mới
        productAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close the database when the activity is destroyed
        dbHelper.close();
    }
    private void openEditProductActivity(int productId) {
        // Mở trang EditProductActivity và chuyển ID sản phẩm cần chỉnh sửa
        Intent intent = new Intent(this, EditProductActivity.class);
        intent.putExtra("productId", productId);
        startActivity(intent);
    }


}