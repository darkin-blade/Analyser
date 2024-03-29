package com.example.analyser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Analyser extends AppCompatActivity {
    static int item_height;
    static int name_padding;
    static int type_padding;
    static int screen_width;
    static int size_padding;

    long total_size;// 目录下文件总大小
    int isExit;

    TextView curPath;
    Button exitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.analyser_layout);

        // 检查权限
        String permission = "android.permission.WRITE_EXTERNAL_STORAGE";
        int check_result = ActivityCompat.checkSelfPermission(this, permission);// `允许`返回0,`拒绝`返回-1
        if (check_result != PackageManager.PERMISSION_GRANTED) {// 没有`写`权限
            ActivityCompat.requestPermissions(this, new String[]{permission}, 1);// 获取`写`权限
        }

        initNum();
        initView();

        File tempFile = getExternalFilesDir("");
        String path = tempFile.getParent();
        for (int i = 0; i < 3 ; i ++) {
            tempFile = new File(path);
            path = tempFile.getParent();
        }
        readPath(path);// TODO 根目录
    }

    public void initView() {
        curPath = findViewById(R.id.cur_path);
        exitBtn = findViewById(R.id.exit_button);// 退出键
        exitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isExit = 1;
                onBackPressed();
            }
        });
    }

    public void initNum() {
        item_height = 130;
        name_padding = 35;
        type_padding = 20;
        size_padding = 5;
        screen_width = getWindowManager().getDefaultDisplay().getWidth() - item_height;// 获取屏幕宽度
        isExit = 0;
    }

    @Override
    public void onBackPressed() {
        if (isExit == 1) {
            super.onBackPressed();
        }
    }

    public static long getSize(File file) {// 计算文件夹大小
        long size = file.length();// 文件夹也有大小
        if (file.isFile()) {
            return size;
        }

        File[] items = file.listFiles();
        if (items == null) {// 目录是否非空
            return size;
        }

        for (int i = 0; i < items.length ; i ++) {
            size += getSize(items[i]);// TODO
        }
        return size;
    }

    public void readPath(String dirPath) {
        if (dirPath == null) {
            info(this, "can't access this path");
            return;
        }

        LinearLayout layout = findViewById(R.id.item_list);
        layout.removeAllViews();

        // 显示路径
        curPath.setText(dirPath);

        // 遍历文件夹
        File dir = new File(dirPath);
        File[] files = dir.listFiles();

        if (files == null) {// 该目录没有文件
            total_size = 1;
            createItem(2, "..", dirPath, total_size);// 父目录
            return;
        }

        // 计算目录总大小
        total_size = 0;
        Item[] items = new Item[files.length];// 用于排序
        for (int i = 0; i < files.length ; i ++) {
            items[i] = new Item();
            items[i].size = getSize(files[i]);// 记录大小
            items[i].file = files[i];// 用于获取名字
            total_size += items[i].size;
        }

        // 按大小排序
        Collections.sort(Arrays.asList(items), new Comparator<Item>() {// TODO
            @Override
            public int compare(Item item1, Item item2) {// 降序
                if (item1.size < item2.size) {
                    return 1;
                } else if (item1.size > item2.size) {
                    return -1;
                }
                return 0;// 需要考虑相等
            }
        });

        if (total_size == 0) {// TODO 不知道什么情况
            total_size = 1;
        }
        createItem(2, "..", dirPath, total_size);// 父目录

        for (int i = 0; i < items.length ; i ++) {
            if (items[i].file.isDirectory()) {
                createItem(1, items[i].file.getName(), dirPath, items[i].size);
            } else {
                createItem(0, items[i].file.getName(), dirPath, items[i].size);
            }
        }
    }

    static public void info(Context context, String log) {
        Toast toast =  Toast.makeText(context, log, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.setBackgroundResource(R.drawable.toast);
        TextView textView = view.findViewById(android.R.id.message);
        textView.setTextColor(Color.rgb(0xff, 0xff, 0xff));
        toast.show();
    }

    private LinearLayout createItem(int itemType, final String itemName, final String itemPath, long size) {// 创建图标
        Log.i("fuck", screen_width + "/" + size + "/" + total_size);

        LinearLayout layout = findViewById(R.id.item_list);
        LinearLayout.LayoutParams itemParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, item_height);
        LinearLayout.LayoutParams typeParam = new LinearLayout.LayoutParams(item_height, item_height);
        LinearLayout.LayoutParams iconParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams rParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams sizeParam = new LinearLayout.LayoutParams((int)((float)screen_width * size / total_size) , LinearLayout.LayoutParams.MATCH_PARENT);
        LinearLayout.LayoutParams nameParam = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        LinearLayout item = new LinearLayout(this);
        item.setLayoutParams(itemParam);
        item.setBackgroundResource(R.color.grey);
        item.setPadding(0, 0, 0, 0);

        LinearLayout type = new LinearLayout(this);// 图标的外圈
        type.setLayoutParams(typeParam);
        type.setPadding(type_padding, type_padding, type_padding, type_padding);

        View icon = new View(this);// 图标
        icon.setLayoutParams(iconParam);
        if (itemType == 0) {// 文件
            icon.setBackgroundResource(R.drawable.item_file);
        } else {// 文件夹
            icon.setBackgroundResource(R.drawable.item_dir);
        }

        RelativeLayout relativeLayout = new RelativeLayout(this);// 使大小显示为文件名的背景
        rParam.setMargins(size_padding, size_padding, size_padding, size_padding);
        relativeLayout.setLayoutParams(rParam);
        relativeLayout.setBackgroundResource(R.color.grey_light);

        View itemSize = new View(this);// 文件大小
        itemSize.setLayoutParams(sizeParam);
        itemSize.setBackgroundResource(R.color.color_1);

        TextView name = new TextView(this);// 文件名
        name.setLayoutParams(nameParam);
        name.setBackgroundResource(R.color.transparent);// 必须透明
        name.setText(itemName);
        name.setPadding(name_padding, name_padding, name_padding, name_padding);
        name.setSingleLine();

        type.addView(icon);
        item.addView(type);
        relativeLayout.addView(itemSize);
        relativeLayout.addView(name);
        item.addView(relativeLayout);

        if (itemType == 2) {// 父文件夹
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File dir = new File(itemPath);
                    readPath(dir.getParent());
                }
            });
        } else if (itemType == 1) {// `点击`遍历子文件夹
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    readPath(itemPath + "/" + itemName);
                }
            });
        }

        layout.addView(item);

        return item;
    }
}
