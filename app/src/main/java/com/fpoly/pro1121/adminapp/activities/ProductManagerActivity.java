package com.fpoly.pro1121.adminapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.fpoly.pro1121.adminapp.R;
import com.fpoly.pro1121.adminapp.adapter.ProductAdapter;
import com.fpoly.pro1121.adminapp.model.Category;
import com.fpoly.pro1121.adminapp.model.Product;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProductManagerActivity extends AppCompatActivity {

    RecyclerView rvProduct;
    FloatingActionButton  fabAddProduct;
    ProductAdapter productAdapter;
    List<Product> list;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_manager);
        initUI();
        initRecyclerView();
        actionAddProduct();
        readDataRealtime();
    }

    private void readDataRealtime() {
        ProgressDialog progressDialog = new ProgressDialog(ProductManagerActivity.this);
        progressDialog.setMessage("loading....");
        progressDialog.show();
        db.collection("products")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value != null){
                            try {
                                List<Product> clones = new ArrayList<>();
                                List<DocumentSnapshot> snapshotsList = value.getDocuments();
                                for(DocumentSnapshot snapshot:  snapshotsList){
                                    Map<String, Object> data = snapshot.getData();
                                    assert data != null;
                                    String id = (String) data.get("id");
                                    String name = (String) data.get("name");
                                    int price =( (Long) data.get("price")).intValue();
                                    String categoryID = (String) data.get("IdCategory");
                                    String urlImage = (String) data.get("urlImage");
                                    String description = (String) data.get("description");
                                    Product product = new Product(id,urlImage,name,price,description,categoryID);
                                    clones.add(product);
                                }
                                list = new ArrayList<>();
                                list.addAll(clones);
                                productAdapter.setData(list);
                                progressDialog.dismiss();
                            }catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

    }

    private void initRecyclerView() {
        productAdapter = new ProductAdapter(new ProductAdapter.IClickPressedListener() {
            @Override
            // khi click show 1 alerdialog xác nhận xoá ( truyền 1 productID)
            public void clickDelete(String productID) {
            new AlertDialog.Builder(ProductManagerActivity.this)
                    .setTitle("Xác nhận")
                    .setMessage("Bạn có thật sự muốn xoá không ?")
                    .setPositiveButton("Xoá", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // xoá product ở đây
                            // nhận 1 productID rồi xoá trên server
                            deleteProduct(productID);
                        }
                    })
                    .setNegativeButton("Huỷ",null)
                    .show();
            }

            @Override
            public void clickUpdate(Product product) {
                Intent intent = new Intent("update",null,ProductManagerActivity.this,AddProductActivity.class);
                intent.putExtra("product",product);
                startActivity(intent);

            }
        });
        productAdapter.setData(list);
        LinearLayoutManager linearLayout =new LinearLayoutManager(this);
        rvProduct.setLayoutManager(linearLayout);
        rvProduct.setAdapter(productAdapter);
    }

    private void deleteProduct(String productID) {
        //Collection dữ liệu từ Firebase, xóa Product dựa vào productID truyền vào
        db.collection("products").document(productID)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(ProductManagerActivity.this, "Đã xóa sản phẩm !", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("=====>", "deleteProduct() Failed !", e);
                    }
                });
    }

    private void actionAddProduct() {
        fabAddProduct.setOnClickListener(view ->{

            startActivity(new Intent("add",null,ProductManagerActivity.this,AddProductActivity.class));
        });
    }


    private void initUI() {
        rvProduct = findViewById(R.id.rv_product);
        fabAddProduct = findViewById(R.id.fab_add_product);
    }
}