# Hướng dẫn Thiết kế JellyPlex (Phase 1)

## 1. Nguyên tắc cốt lõi (Core Principles)

- **Clean Architecture:** Tách biệt rõ rệt giữa Data, Domain và UI layers.
- **Single Responsibility Principle (SRP):** Mỗi file/class chỉ làm một việc duy nhất.
- **Limit:** Tối đa 300 dòng cho mỗi file `.kt`. Nếu vượt quá, bắt buộc phải tách component hoặc logic.

## 2. Kiến trúc Giao diện (Adaptive UI)

- **Shared Components:** Xây dựng component ở `ui/common/components` để dùng cho cả Mobile và Desktop.
- **D-pad Support:**
    - Mọi tương tác click (`Modifier.clickable`) phải đi kèm với xử lý Focus cho Desktop/TV.
    - Sử dụng `Modifier.onFocusChanged` và hiển thị Border/Highlight rõ rệt khi `isFocused == true`.
- **UI Dispatching:** Sử dụng `MainScreen` làm bộ điều phối dựa trên `UiType`.

## 3. Quản lý trạng thái (State Management)

- Sử dụng ViewModels từ `commonMain`.
- Không để logic nghiệp vụ (business logic) nằm trong các hàm Composable.
