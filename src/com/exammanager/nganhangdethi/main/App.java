package com.exammanager.nganhangdethi.main;

import com.exammanager.nganhangdethi.gui.MainAppFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        
        try {
            
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
           
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Không thể thiết lập Look and Feel của hệ thống: " + ex.getMessage());
                
            }
        }

        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Khởi tạo và hiển thị cửa sổ chính
                MainAppFrame mainFrame = new MainAppFrame();
                mainFrame.setVisible(true);
            }
        });
    }
}
