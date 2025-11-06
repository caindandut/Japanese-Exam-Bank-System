package com.exammanager.nganhangdethi.service;

import com.exammanager.nganhangdethi.dao.LevelDAO;
import com.exammanager.nganhangdethi.model.Level;

import java.util.List;

/**
 * Lớp Service cho việc quản lý Cấp độ (Levels).
 * Đóng vai trò trung gian giữa GUI và LevelDAO, chứa logic nghiệp vụ (nếu có).
 */
public class LevelService {

    private LevelDAO levelDAO;

    public LevelService() {
        this.levelDAO = new LevelDAO(); // Khởi tạo DAO
    }

    /**
     * Lấy tất cả các cấp độ.
     * @return Danh sách các Level.
     */
    public List<Level> getAllLevels() {
        // Hiện tại, chỉ gọi trực tiếp DAO.
        // Trong tương lai, có thể có thêm logic ở đây (ví dụ: kiểm tra quyền, logging, caching).
        return levelDAO.getAllLevels();
    }

    /**
     * Lấy một cấp độ theo ID.
     * @param levelId ID của cấp độ.
     * @return Đối tượng Level hoặc null nếu không tìm thấy.
     */
    public Level getLevelById(long levelId) {
        return levelDAO.getLevelById(levelId);
    }

    /**
     * Thêm một cấp độ mới.
     * @param level Đối tượng Level cần thêm (chỉ cần LevelName).
     * @return true nếu thêm thành công, false nếu thất bại.
     * Đối tượng level sẽ được cập nhật ID sau khi thêm.
     * @throws IllegalArgumentException nếu LevelName trống hoặc null.
     */
    public boolean addLevel(Level level) throws IllegalArgumentException {
        if (level == null || level.getLevelName() == null || level.getLevelName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên cấp độ không được để trống.");
        }
        // Có thể thêm logic kiểm tra trùng tên ở đây nếu cần
        // Ví dụ: if (levelDAO.findByName(level.getLevelName()) != null) { throw new Exception("Tên cấp độ đã tồn tại"); }
        return levelDAO.addLevel(level);
    }

    /**
     * Cập nhật một cấp độ.
     * @param level Đối tượng Level cần cập nhật (phải có LevelID và LevelName mới).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     * @throws IllegalArgumentException nếu LevelName trống hoặc null, hoặc LevelID không hợp lệ.
     */
    public boolean updateLevel(Level level) throws IllegalArgumentException {
        if (level == null || level.getLevelID() <= 0) {
            throw new IllegalArgumentException("ID cấp độ không hợp lệ để cập nhật.");
        }
        if (level.getLevelName() == null || level.getLevelName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên cấp độ không được để trống khi cập nhật.");
        }
        // Logic kiểm tra trùng tên (ngoại trừ chính nó) có thể thêm ở đây
        return levelDAO.updateLevel(level);
    }

    /**
     * Xóa một cấp độ.
     * @param levelId ID của cấp độ cần xóa.
     * @return true nếu xóa thành công, false nếu thất bại.
     * @throws Exception nếu cấp độ này đang được sử dụng (ví dụ trong bảng Questions)
     * và không thể xóa do ràng buộc khóa ngoại (cần xử lý ở DAO hoặc DB).
     */
    public boolean deleteLevel(long levelId) throws Exception {
        // Trước khi xóa, có thể cần kiểm tra xem Level này có đang được tham chiếu ở đâu không.
        // Ví dụ: kiểm tra trong bảng Questions. Nếu có, không cho xóa hoặc hỏi người dùng.
        // Hiện tại, LevelDAO sẽ trả về false (và in lỗi) nếu có lỗi khóa ngoại.
        // Lớp Service có thể bắt lỗi đó và thông báo cụ thể hơn.
        boolean deleted = levelDAO.deleteLevel(levelId);
        if (!deleted) {
            // Có thể ném một Exception cụ thể hơn ở đây nếu muốn GUI xử lý
            // throw new Exception("Không thể xóa cấp độ. Có thể cấp độ này đang được sử dụng.");
        }
        return deleted;
    }

    // Phương thức main để kiểm tra nhanh (tùy chọn)
    public static void main(String[] args) {
        LevelService levelService = new LevelService();

        System.out.println("Lấy tất cả Levels từ Service:");
        List<Level> levels = levelService.getAllLevels();
        if (levels.isEmpty()) {
            System.out.println("Chưa có level nào. Thêm một vài level mẫu để test.");
            try {
                levelService.addLevel(new Level("N5 Service Test"));
                levelService.addLevel(new Level("N4 Service Test"));
                levels = levelService.getAllLevels();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }

        for (Level l : levels) {
            System.out.println("ID: " + l.getLevelID() + ", Name: " + l.getLevelName());
        }
        System.out.println("-----");

        // Thử thêm mới
        try {
            Level newLevel = new Level("N3 Service Test");
            if (levelService.addLevel(newLevel)) {
                System.out.println("Thêm thành công Level từ Service: " + newLevel.getLevelName() + " (ID: " + newLevel.getLevelID() + ")");

                // Thử cập nhật
                newLevel.setLevelName("N3 Service Test - Updated");
                if (levelService.updateLevel(newLevel)) {
                    System.out.println("Cập nhật thành công. Tên mới: " + newLevel.getLevelName());
                } else {
                    System.out.println("Cập nhật thất bại.");
                }

                // Thử xóa
                System.out.println("Đang xóa Level ID: " + newLevel.getLevelID());
                if (levelService.deleteLevel(newLevel.getLevelID())) {
                    System.out.println("Xóa thành công.");
                } else {
                    System.out.println("Xóa thất bại (có thể do đang được sử dụng hoặc lỗi khác).");
                }

            } else {
                System.out.println("Thêm Level từ Service thất bại.");
            }
        } catch (Exception e) {
            System.err.println("Lỗi trong quá trình test LevelService: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
