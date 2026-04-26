# Hướng dẫn sử dụng Backend - Milk Tea Management

Tài liệu này tổng hợp cách sử dụng các Repositories và Managers để kết nối giao diện (UI) với Firebase.

---

## 1. Quản lý Giỏ hàng (`CartManager`)
Sử dụng Singleton này để lưu trữ món ăn tạm thời trước khi đặt hàng.

- **Lấy instance:** `CartManager.getInstance()`
- **Thêm món:** 
  ```java
  CartItem item = new CartItem(productId, productName, quantity, size, toppings, subTotal);
  CartManager.getInstance().addToCart(item);
  ```
- **Lấy tổng tiền:** `double total = CartManager.getInstance().getTotalAmount();`
- **Xóa giỏ hàng:** `CartManager.getInstance().clearCart();`

---

## 2. Quản lý Sản phẩm & Đặt hàng (`FirebaseService`)
Chứa các logic nghiệp vụ chính của App.

### Lấy danh sách món ăn:
```java
firebaseService.getAllProducts(new FirebaseService.OnProductsLoadedListener() {
    @Override
    public void onSuccess(List<Product> products) {
        // Cập nhật RecyclerView tại đây
    }
    @Override
    public void onFailure(Exception e) {
        // Hiện thông báo lỗi
    }
});
```

### Đặt hàng (Checkout):
```java
Order newOrder = new Order(orderId, userId, CartManager.getInstance().getCartItems(), total, FirebaseConstants.STATUS_PENDING, timestamp, note);
firebaseService.placeOrder(newOrder, ...);
```

### Theo dõi trạng thái đơn hàng (Real-time):
Hệ thống sẽ tự động hiện Toast và rung khi trạng thái đơn hàng thay đổi.
```java
firebaseService.startListeningOrder(orderId);
```

---

## 3. Các Repositories chuyên biệt

### `MenuRepository` (Dành cho Admin)
- `upsertProduct(Product, callback)`: Thêm hoặc cập nhật món.
- `deleteProduct(productId, callback)`: Xóa món.

### `ProfileRepository`
- `updateUserInfo(userId, name, phone, address, callback)`: Cập nhật thông tin cá nhân.

### `OrderHistoryRepository`
- `fetchUserOrderHistory(customerId, callback)`: Lấy danh sách đơn hàng đã đặt của khách.

### `AdminAnalyticsRepository`
- `getBestSellingProduct(callback)`: Tìm món bán chạy nhất dựa trên các đơn `SHIPPED`.

---

## 4. Cấu trúc dữ liệu trả về (`Resource` class)
Dùng trong ViewModel để quản lý trạng thái UI.

```java
// Ví dụ logic trong ViewModel
MutableLiveData<Resource<List<Product>>> productsData = new MutableLiveData<>();
productsData.setValue(Resource.loading(null));

// Nếu thành công
productsData.setValue(Resource.success(list));

// Nếu lỗi
productsData.setValue(Resource.error("Lỗi kết nối", null));
```

---

## 5. Các hằng số (`FirebaseConstants`)
Luôn sử dụng hằng số thay vì viết chữ trực tiếp:
- `FirebaseConstants.COL_ORDERS`: "Orders"
- `FirebaseConstants.STATUS_PENDING`: "PENDING"
- `FirebaseConstants.STATUS_SHIPPED`: "SHIPPED"
- ...
