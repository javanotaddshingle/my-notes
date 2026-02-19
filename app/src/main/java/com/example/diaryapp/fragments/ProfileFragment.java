/*
"我的"页面更换头像和背景功能
 */


package com.example.diaryapp.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.example.diaryapp.R;
import com.example.diaryapp.activities.SettingsActivity;
import com.example.diaryapp.managers.UserManager;
import com.example.diaryapp.models.User;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.util.Log;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";

    private TextView userNameTextView;
    private TextView userLevelTextView;
    private TextView userExperienceTextView;
    private TextView experienceToNextLevelTextView;
    private ProgressBar experienceProgressBar;
    private UserManager userManager;
    private boolean isLoading = false;

    // 新增：用户头像和背景照片相关
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_BACKGROUND_REQUEST = 2;

    // 新增：Activity Result Launchers
    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<Intent> cropLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    // 新增：存储原始请求码和临时裁剪文件
    private int lastOriginalRequestCode = -1;
    private Uri cropImageUri; // 裁剪后的图片URI

    // 新增：UI元素引用
    private ImageView avatarImageView;
    private ImageView backgroundImageView;
    private TextView userSignatureTextView;
    private TextView userIdTextView;
//    private Button changeAvatarButton;
//    private Button changeBackgroundButton;
//    private Button editProfileButton;
    // 新增LinearLayout声明（和新布局ID对应）
    private LinearLayout changeAvatarLayout, changeBackgroundLayout, editProfileLayout, settingsLayout;

    // 修复：添加生命周期绑定的Handler
    private android.os.Handler handler = new android.os.Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 修复：绑定Activity的Looper
        handler = new android.os.Handler(requireActivity().getMainLooper());
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // 初始化所有Activity Result Launchers（修复核心问题1）
        initActivityResultLaunchers();

        // 原有元素
        userNameTextView = view.findViewById(R.id.user_name);
        userLevelTextView = view.findViewById(R.id.user_level);
        userExperienceTextView = view.findViewById(R.id.user_experience);
        experienceToNextLevelTextView = view.findViewById(R.id.experience_to_next_level);
        experienceProgressBar = view.findViewById(R.id.experience_progress);

        // 新增元素
        avatarImageView = view.findViewById(R.id.avatar_image);
        backgroundImageView = view.findViewById(R.id.background_image);
        userSignatureTextView = view.findViewById(R.id.user_signature);
        userIdTextView = view.findViewById(R.id.user_id);
//        changeAvatarButton = view.findViewById(R.id.change_avatar_button);
//        changeBackgroundButton = view.findViewById(R.id.change_background_button);
//        editProfileButton = view.findViewById(R.id.edit_profile_button);
        // 新增LinearLayout findViewById（和新布局ID对应）
        changeAvatarLayout = view.findViewById(R.id.change_avatar_layout);
        changeBackgroundLayout = view.findViewById(R.id.change_background_layout);
        editProfileLayout = view.findViewById(R.id.edit_profile_layout);
        settingsLayout = view.findViewById(R.id.settings_layout); // 新增设置入口
//        本质：getInstance是封装的静态方法，内部会判断：
//✅ 首次调用：执行new UserManager(context)创建实例；
//✅ 非首次：直接返回已创建的实例；
//        核心目的：保证 APP 里只有 1 个 UserManager，避免多实例导致用户数据不一致。
        userManager = UserManager.getInstance(requireContext());

        // 设置点击事件
//        changeAvatarButton.setOnClickListener(v -> openImagePicker(PICK_IMAGE_REQUEST));
//        changeBackgroundButton.setOnClickListener(v -> openImagePicker(PICK_BACKGROUND_REQUEST));
//        editProfileButton.setOnClickListener(v -> editUserProfile());
        // 新增LinearLayout点击事件（逻辑和之前完全一样）
        changeAvatarLayout.setOnClickListener(v -> openImagePicker(PICK_IMAGE_REQUEST));
        changeBackgroundLayout.setOnClickListener(v -> openImagePicker(PICK_BACKGROUND_REQUEST));
        editProfileLayout.setOnClickListener(v -> editUserProfile());
        // 新增设置入口点击事件（跳转到SettingsActivity）
        settingsLayout.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), SettingsActivity.class);
            startActivity(intent);
        });
        userSignatureTextView.setOnClickListener(v -> editUserProfile());

        // 首先显示默认值，避免界面空白
        showDefaultUserInfo();

        // 然后异步加载用户信息
        loadUserInfo();

        return view;
    }

    // 修复：完整初始化所有Activity Result Launchers
    private void initActivityResultLaunchers() {
        // 1. 图片选择器
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        try {
                            // 创建临时裁剪文件（修复裁剪URI权限问题）
                            cropImageUri = createImageFileUri();
                            if (cropImageUri != null) {
                                startCropActivity(uri, cropImageUri, lastOriginalRequestCode);
                            } else {
                                Toast.makeText(requireContext(), "创建临时文件失败", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "准备裁剪失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            // 降级处理：不裁剪直接使用
                            handleImageWithoutCrop(uri, lastOriginalRequestCode);
                        }
                    } else {
                        Toast.makeText(requireContext(), "未选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 2. 裁剪活动（修复裁剪回调无响应问题）
        cropLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            if (cropImageUri != null) {
                                // 从裁剪后的URI加载图片
                                String imagePath = saveImageToInternalStorage(cropImageUri);
                                if (imagePath != null) {
                                    if (lastOriginalRequestCode == PICK_IMAGE_REQUEST) {
                                        updateUserAvatar(imagePath);
                                        loadScaledImage(imagePath, avatarImageView, 200, 200);
                                        Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
                                    } else if (lastOriginalRequestCode == PICK_BACKGROUND_REQUEST) {
                                        updateUserBackground(imagePath);
                                        loadScaledImage(imagePath, backgroundImageView, 1080, 720);
                                        backgroundImageView.setVisibility(View.VISIBLE);
                                        Toast.makeText(requireContext(), "背景更新成功", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(requireContext(), "保存图片失败", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireContext(), "处理裁剪图片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Toast.makeText(requireContext(), "取消裁剪", Toast.LENGTH_SHORT).show();
                    }
                    // 重置状态
                    lastOriginalRequestCode = -1;
                    cropImageUri = null;
                }
        );

        // 3. 权限请求（修复权限请求无回调问题）
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // 权限授予，打开图片选择器
                        imagePickerLauncher.launch("image/*");
                    } else {
                        // 权限拒绝
                        Toast.makeText(requireContext(), "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    // 修复：打开图片选择器（使用新的权限请求API）
    private void openImagePicker(int requestCode) {
        try {
            Log.d(TAG, "打开图片选择器: requestCode=" + requestCode);

            // 保存原始请求码
            lastOriginalRequestCode = requestCode;

            // 检查权限
            if (checkStoragePermission()) {
                Log.d(TAG, "权限已授予，启动图片选择器");
                // 权限已授予，直接打开图片选择器
                imagePickerLauncher.launch("image/*");
            } else {
                Log.d(TAG, "权限未授予，请求权限");
                // 使用新的API请求权限
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES);
                } else {
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "打开图片选择器失败", e);
            e.printStackTrace();
            Toast.makeText(requireContext(), "打开相册失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 修复：检查存储权限（适配Android 13+）
    private boolean checkStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return PackageManager.PERMISSION_GRANTED == requireContext().checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES);
        } else {
            return PackageManager.PERMISSION_GRANTED == requireContext().checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    // 修复：创建临时图片文件URI（解决裁剪权限问题）
    private Uri createImageFileUri() throws IOException {
        // 创建临时文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "CROP_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(
                imageFileName,  /* 前缀 */
                ".jpg",         /* 后缀 */
                storageDir      /* 存储目录 */
        );

        // 使用FileProvider生成安全的URI（适配Android 7.0+）
        return FileProvider.getUriForFile(
                requireContext(),
                "com.example.diaryapp.fileprovider", // 替换为你的FileProvider授权名
                imageFile
        );
    }

    // 修复：启动裁剪活动（兼容更多设备）
    private void startCropActivity(Uri sourceUri, Uri outputUri, int originalRequestCode) {
        try {
            Log.d(TAG, "启动裁剪活动: sourceUri=" + sourceUri + ", originalRequestCode=" + originalRequestCode);
            Log.d(TAG, "创建临时输出文件: outputUri=" + outputUri);

            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            // 添加URI访问权限
            cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            cropIntent.setDataAndType(sourceUri, "image/*");
            cropIntent.putExtra("crop", "true");

            // 根据请求类型设置裁剪参数
            if (originalRequestCode == PICK_IMAGE_REQUEST) {
                // 头像：强制正方形裁剪
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("outputX", 300);
                cropIntent.putExtra("outputY", 300);
                Log.d(TAG, "设置头像裁剪参数: 1:1, 300x300");
            } else if (originalRequestCode == PICK_BACKGROUND_REQUEST) {
                // 背景：宽高比3:2
                cropIntent.putExtra("aspectX", 3);
                cropIntent.putExtra("aspectY", 2);
                cropIntent.putExtra("outputX", 1080);
                cropIntent.putExtra("outputY", 720);
                Log.d(TAG, "设置背景裁剪参数: 3:2, 1080x720");
            }

            // 关键修复：改用系统标准常量，删除自定义MediaStore
            cropIntent.putExtra("output", outputUri);
            cropIntent.putExtra("scale", true);
            cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            cropIntent.putExtra("return-data", false); // 关闭return-data

            // 检查是否有应用可以处理裁剪Intent
            if (cropIntent.resolveActivity(requireContext().getPackageManager()) != null) {
                Log.d(TAG, "启动裁剪Intent");
                cropLauncher.launch(cropIntent);
            } else {
                // 没有裁剪应用，直接使用原图
                Log.e(TAG, "没有可用的裁剪应用");
//                Toast.makeText(requireContext(), "没有可用的裁剪应用，将使用原图", Toast.LENGTH_SHORT).show();
                handleImageWithoutCrop(sourceUri, originalRequestCode);
            }
        } catch (Exception e) {
            Log.e(TAG, "启动裁剪活动失败", e);
            e.printStackTrace();
            Toast.makeText(requireContext(), "启动裁剪失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            handleImageWithoutCrop(sourceUri, originalRequestCode);
        }
    }

    // 优化：当裁剪失败时直接使用原图（增加错误提示）
    private void handleImageWithoutCrop(Uri uri, int originalRequestCode) {
        try {
            String imagePath = saveImageToInternalStorage(uri);
            if (imagePath != null) {
                if (originalRequestCode == PICK_IMAGE_REQUEST) {
                    updateUserAvatar(imagePath);
                    loadScaledImage(imagePath, avatarImageView, 200, 200);
                    Toast.makeText(requireContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
                } else if (originalRequestCode == PICK_BACKGROUND_REQUEST) {
                    updateUserBackground(imagePath);
                    loadScaledImage(imagePath, backgroundImageView, 1080, 720);
                    backgroundImageView.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "背景更新成功", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "保存图片失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "处理图片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 优化：保存图片到内部存储（增加异常提示）
    private String saveImageToInternalStorage(Uri uri) {
        try {
            Log.d(TAG, "开始保存图片到内部存储: uri=" + uri);

            // 解码图片时使用BitmapFactory.Options来减少内存使用
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();

            Log.d(TAG, "图片尺寸: " + options.outWidth + "x" + options.outHeight);

            // 计算采样率
            options.inSampleSize = calculateInSampleSize(options, 1024, 1024);
            options.inJustDecodeBounds = false;

            Log.d(TAG, "使用采样率: " + options.inSampleSize);

            // 重新解码图片
            inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

            if (bitmap == null) {
                Log.e(TAG, "图片解码失败，返回null");
                Toast.makeText(requireContext(), "图片解码失败", Toast.LENGTH_SHORT).show();
                return null;
            }

            Log.d(TAG, "图片解码成功: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            File directory = new File(requireContext().getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdirs();
                Log.d(TAG, "创建图片目录: " + directory.getAbsolutePath());
            }

            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            // 修复：删除手动回收Bitmap的代码
            Log.d(TAG, "图片保存成功: " + file.getAbsolutePath());
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "保存图片到内部存储失败", e);
            e.printStackTrace();
            Toast.makeText(requireContext(), "保存图片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // 优化：保存Bitmap到内部存储（增加异常提示）
    private String saveBitmapToInternalStorage(Bitmap bitmap) {
        try {
            File directory = new File(requireContext().getFilesDir(), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "img_" + System.currentTimeMillis() + ".jpg";
            File file = new File(directory, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
            outputStream.flush();
            outputStream.close();

            // 修复：删除手动回收Bitmap的代码
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "保存图片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    // 原有：计算图片采样率
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // 优化：加载缩放后的图片到ImageView（修复内存泄漏）
    private void loadScaledImage(String imagePath, ImageView imageView, int maxWidth, int maxHeight) {
        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                Toast.makeText(requireContext(), "图片文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            // 解码图片时使用BitmapFactory.Options来减少内存使用
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(imagePath, options);

            // 计算采样率
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
            options.inJustDecodeBounds = false;

            // 解码缩放后的图片
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

            if (bitmap != null) {
                // 对于头像，确保是正方形
                if (imageView == avatarImageView) {
                    // 创建正方形的Bitmap
                    int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
                    int x = (bitmap.getWidth() - size) / 2;
                    int y = (bitmap.getHeight() - size) / 2;
                    Bitmap squareBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
                    imageView.setImageBitmap(squareBitmap);
                    // 修复：删除手动回收Bitmap的代码
                } else {
                    imageView.setImageBitmap(bitmap);
                }
            } else {
                Toast.makeText(requireContext(), "加载图片失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "加载图片失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 原有：更新用户头像
    private void updateUserAvatar(String avatarPath) {
        userManager.updateUserAvatar(avatarPath);
    }

    // 原有：更新用户背景照片
    private void updateUserBackground(String backgroundPath) {
        userManager.updateUserBackground(backgroundPath);
    }

    // 原有：编辑用户资料
    private void editUserProfile() {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("编辑个人资料");

            View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null);
            builder.setView(view);

            EditText nameEditText = view.findViewById(R.id.name_edit_text);
            EditText signatureEditText = view.findViewById(R.id.signature_edit_text);

            // 先显示默认值
            nameEditText.setText("用户");

            // 在后台线程中获取用户信息
            new Thread(() -> {
                User user = userManager.getUser();
                // 在UI线程上更新对话框
                requireActivity().runOnUiThread(() -> {
                    if (user != null) {
                        nameEditText.setText(user.getName());
                        if (user.getSignature() != null) {
                            signatureEditText.setText(user.getSignature());
                        }
                    }
                });
            }).start();

            builder.setPositiveButton("保存", (dialog, which) -> {
                String newName = nameEditText.getText().toString().trim();
                String newSignature = signatureEditText.getText().toString().trim();

                if (!newName.isEmpty()) {
                    updateUserProfile(newName, newSignature);
                } else {
                    Toast.makeText(requireContext(), "用户名不能为空", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("取消", null);
            builder.show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "打开编辑对话框失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // 原有：更新用户资料
    private void updateUserProfile(String name, String signature) {
        // 更新用户名
        userManager.updateUserName(name);
        // 更新个性签名
        userManager.updateUserSignature(signature);

        // 延迟一点时间再重新加载用户信息，确保数据库操作已完成
        handler.postDelayed(() -> {
            loadUserInfo();
        }, 500);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo(); // 每次返回页面时重新加载数据
    }

    // 修复：移除Handler回调，避免内存泄漏
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }

    private void showDefaultUserInfo() {
        userNameTextView.setText("用户");
        userLevelTextView.setText("等级: 1");
        userExperienceTextView.setText("经验值: 0");
        experienceToNextLevelTextView.setText("/ 100");
        experienceProgressBar.setMax(100);
        experienceProgressBar.setProgress(0);
    }

    private void loadUserInfo() {
        // 避免重复加载
        if (isLoading) {
            return;
        }

        isLoading = true;

        // 使用单个线程来执行数据库操作，避免嵌套线程
        new Thread(() -> {
            try {
                // 直接获取用户信息
                final User user = userManager.getUser();

                // 更新UI
                requireActivity().runOnUiThread(() -> {
                    try {
                        if (user != null) {
                            // 原有信息
                            userNameTextView.setText(user.getName());
                            userLevelTextView.setText("等级: " + user.getLevel());
                            userExperienceTextView.setText("经验值: " + user.getExperiencePoints());
                            experienceToNextLevelTextView.setText("/ " + user.getExperienceToNextLevel());
                            experienceProgressBar.setMax(user.getExperienceToNextLevel());
                            experienceProgressBar.setProgress(user.getExperiencePoints());

                            // 新增信息
                            userIdTextView.setText("ID: " + user.getId());

                            // 加载头像
                            if (user.getAvatarPath() != null) {
                                File avatarFile = new File(user.getAvatarPath());
                                if (avatarFile.exists()) {
                                    loadScaledImage(user.getAvatarPath(), avatarImageView, 200, 200);
                                }
                            }

                            // 加载背景照片
                            if (user.getBackgroundPath() != null) {
                                File backgroundFile = new File(user.getBackgroundPath());
                                if (backgroundFile.exists()) {
                                    loadScaledImage(user.getBackgroundPath(), backgroundImageView, 1080, 720);
                                    backgroundImageView.setVisibility(View.VISIBLE);
                                }
                            }

                            // 加载个性签名
                            if (user.getSignature() != null && !user.getSignature().isEmpty()) {
                                userSignatureTextView.setText(user.getSignature());
                            } else {
                                userSignatureTextView.setText("点击编辑个性签名");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "加载用户信息失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    } finally {
                        isLoading = false;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "加载用户信息失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                });
            }
        }).start();
    }
}