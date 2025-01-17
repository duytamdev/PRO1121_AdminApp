package com.fpoly.pro1121.adminapp.activities;


import static com.fpoly.pro1121.adminapp.Constant.ALERT_BUTTON_CANCEL;
import static com.fpoly.pro1121.adminapp.Constant.ALERT_BUTTON_DELETE;
import static com.fpoly.pro1121.adminapp.Constant.ALERT_CONFIRM_DELETE;
import static com.fpoly.pro1121.adminapp.Constant.TOAST_DELETE_FAILED;
import static com.fpoly.pro1121.adminapp.Constant.TOAST_DELETE_SUCCESS;
import static com.fpoly.pro1121.adminapp.Constant.TOAST_INSERT_SUCCESS;
import static com.fpoly.pro1121.adminapp.Constant.TOAST_UPDATE_SUCCESS;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fpoly.pro1121.adminapp.R;
import com.fpoly.pro1121.adminapp.Utils;
import com.fpoly.pro1121.adminapp.adapter.CategoryAdapter;
import com.fpoly.pro1121.adminapp.model.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CategoryManagerActivity extends AppCompatActivity {

    Toolbar toolbar;
    RecyclerView rvCategory;
    FloatingActionButton fabAddCategory;
    CategoryAdapter categoryAdapter;
    List<Category> categories;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String urlImage = "https://firebasestorage.googleapis.com/v0/b/project-1-android-teamwork.appspot.com/o/imgCategory%2Fhamburger.png?alt=media&token=acbdd123-075f-470b-8ac2-6c169bb2d7f4";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_manager);

        initUI();
        initToolbar();
        initRecyclerView();
        readDataRealtime();
        events();
    }
    private void initToolbar() {
        toolbar = findViewById(R.id.toolbar_category_manager);
        toolbar.setTitle("Danh Sách Loại Sản Phẩm");
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

    }
    private void readDataRealtime() {
        db.collection("categories")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if(value!=null){
                            try {
                                List<Category> clones = new ArrayList<>();
                                List<DocumentSnapshot> snapshotsList = value.getDocuments();
                                for(DocumentSnapshot snapshot:  snapshotsList){
                                    Map<String, Object> data = snapshot.getData();
                                    assert data != null;
                                    String id = Objects.requireNonNull(data.get("id")).toString();
                                    String name = Objects.requireNonNull(data.get("name")).toString();
                                    String urlImage = Objects.requireNonNull(data.get("urlImage")).toString();
                                    Category category = new Category(id,name,urlImage);
                                    clones.add(category);
                                }
                                categories = new ArrayList<>();
                                categories.addAll(clones);
                                categoryAdapter.setData(categories);
                            }catch(Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    private void initRecyclerView() {
        categoryAdapter = new CategoryAdapter(new CategoryAdapter.IClickCategory() {
            // khi click show 1 alerdialog xác nhận xoá ( truyền 1 categoryID)
            @Override
            public void clickDelete(String categoryID) {
                // click delete category
                new AlertDialog.Builder(CategoryManagerActivity.this)
                        .setTitle("Xác Nhận")
                        .setMessage(ALERT_CONFIRM_DELETE)
                        .setPositiveButton(ALERT_BUTTON_DELETE, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // xoá category ở đây
                                // nhận 1 categoryID rồi xoá trên server
                                deleteCategory(categoryID);

                            }
                        })
                        .setNegativeButton(ALERT_BUTTON_CANCEL,null)
                        .show();

            }

            @Override
            public void clickUpdate(Category category) {
                openDialogAddUpdatedCategory(false,category);
            }
        });
        categoryAdapter.setData(categories);
        rvCategory.setAdapter(categoryAdapter);
        LinearLayoutManager linearLayout = new LinearLayoutManager(this,RecyclerView.VERTICAL,false);
        rvCategory.setLayoutManager(linearLayout);
    }

    private void deleteCategory(String categoryID) {
        db.collection("categories").document(categoryID).delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(CategoryManagerActivity.this,TOAST_DELETE_SUCCESS,Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(CategoryManagerActivity.this,TOAST_DELETE_FAILED,Toast.LENGTH_SHORT).show();
                            Log.e("--->", "onComplete: error");
                        }
                    }
                });
    }


    private void initUI() {
        rvCategory = findViewById(R.id.rv_category);
        fabAddCategory = findViewById(R.id.fab_add_category);
    }

    private void events() {
        fabAddCategory.setOnClickListener(view -> openDialogAddUpdatedCategory(true));
    }

    private void openDialogAddUpdatedCategory(boolean isAdd, Category ...categoryCurrent) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(R.layout.dialog_add_category);
        TextInputLayout tilNameCategory = dialog.findViewById(R.id.til_name_category);
        EditText edtName = dialog.findViewById(R.id.edt_name_category);
        Button btnAdd = dialog.findViewById(R.id.btn_add_category);
        assert edtName != null;
        Utils.addTextChangedListener(edtName,tilNameCategory,false);
        if(isAdd){
            btnAdd.setText("Add");
            btnAdd.setOnClickListener(view -> {
               try {
                   String name = edtName.getText().toString().trim();
                   //check validation
                   if(name.isEmpty()||tilNameCategory.getError()!=null){
                       return;
                   }
                   // get chuỗi random làm id category
                   UUID uuid = UUID.randomUUID();
                   String idCategory = uuid.toString();
                   // tạo 1 đối tượng rồi add vào firebase
                   Category category = new Category(idCategory,name,urlImage);
                   // hàm add
                   addCategory(category);
                   dialog.dismiss();
               }catch(Exception e){
                   e.printStackTrace();
               }
            });
            // khi click done trên bàn phím
            edtName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                    if (i == EditorInfo.IME_ACTION_DONE) {
                        try {
                            String name = edtName.getText().toString().trim();
                            //check validation
                            if(name.isEmpty()||tilNameCategory.getError()!=null){
                                Log.e("-->", "onEditorAction:error" );
                            }else{
                                // get chuỗi random làm id category
                                UUID uuid = UUID.randomUUID();
                                String idCategory = uuid.toString();
                                // tạo 1 đối tượng rồi add vào firebase
                                Category category = new Category(idCategory,name,urlImage);
                                // hàm add
                                addCategory(category);
                            }

                            dialog.dismiss();
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
        else{
            try {
                btnAdd.setText("Update");
                edtName.setText(categoryCurrent[0].getName());
                btnAdd.setOnClickListener(view -> {
                    String name = edtName.getText().toString();
                    categoryCurrent[0].setName(name);
                    if(name.isEmpty()||tilNameCategory.getError()!=null){
                        return;
                    }
                    updateCategory(categoryCurrent[0]);
                    dialog.dismiss();
                });
                // khi click done trên bàn phím
                edtName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                        if(i== EditorInfo.IME_ACTION_DONE){
                            String name = edtName.getText().toString();
                            categoryCurrent[0].setName(name);
                            if(name.isEmpty()||tilNameCategory.getError()!=null){
                                Log.e("-->", "onEditorAction:error" );
                            }else{
                                updateCategory(categoryCurrent[0]);
                                dialog.dismiss();
                            }

                            return true;
                        }
                        return false;
                    }
                });

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        dialog.show();
    }

    // chỉ update tên category, ko update img
    // update thành công xuất ra thông báo
    private void updateCategory(Category category) {
        db.collection("categories")
                .document(category.getId())
                .update("name", category.getName()).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(CategoryManagerActivity.this, TOAST_UPDATE_SUCCESS, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // nhận 1 category
    private void addCategory(Category category) {
        // tạo 1 document trùng với categoryID
        db.collection("categories")
                .document(category.getId())
                .set(category)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(CategoryManagerActivity.this, TOAST_INSERT_SUCCESS, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("--->","onFailure:"));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_right);
    }

}