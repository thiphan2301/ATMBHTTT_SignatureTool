import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;

public class SignatureToolUI extends JFrame {

    public SignatureToolUI() {
        setTitle("Tool Chữ Ký Điện Tử Đơn Hàng");
        setSize(650, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
        
        // Thêm padding cho toàn bộ Tab
        UIManager.put("TabbedPane.contentBorderInsets", new Insets(10, 10, 10, 10));

        // Dùng JTabbedPane chia các chức năng thành các Tab 
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Tạo Cặp Khóa", createKeyGenPanel());
        tabbedPane.addTab("Ký Điện Tử", createSignPanel());

        add(tabbedPane);
    }

    // View cho chức năng tạo cặp khóa
    private JPanel createKeyGenPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        JButton btnGenerate = new JButton("Tạo Khóa (Public & Private Key)");
        JLabel lblStatus = new JLabel("Nhấn nút để tạo và chọn nơi lưu file .txt");

        btnGenerate.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                try {
                    SecurityUtil.generateAndSaveKeys(selectedFolder.getAbsolutePath());
                    lblStatus.setText("Đã lưu public_key.txt và private_key.txt tại: " + selectedFolder.getAbsolutePath());
                    lblStatus.setForeground(Color.BLUE);
                    JOptionPane.showMessageDialog(this, "Tạo khóa thành công!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(btnGenerate, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(15, 0, 0, 0); // Tạo khoảng cách
        panel.add(lblStatus, gbc);
        
        return panel;
    }

    // view cho chức năng ký (sign)
    private JPanel createSignPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 5, 8, 5); // Khoảng cách giữa các phần tử
        gbc.weightx = 1.0;

        // Chuỗi thông tin đh
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Nhập Chuỗi Thông Tin Đơn Hàng:"), gbc);
        gbc.gridy = 1;
        JTextArea txtOrderInput = new JTextArea(3, 20);
        txtOrderInput.setLineWrap(true);
        txtOrderInput.setWrapStyleWord(true);
        panel.add(new JScrollPane(txtOrderInput), gbc);
        
        // Chọn file Private Key
        gbc.gridy = 2; gbc.gridwidth = 1; gbc.weightx = 0.2;
        JButton btnChooseKey = new JButton("Chọn file Private Key");
        panel.add(btnChooseKey, gbc);

        gbc.gridx = 1; gbc.weightx = 0.8;
        JLabel lblKeyPath = new JLabel("Chưa chọn file (.txt)");
        lblKeyPath.setForeground(Color.GRAY);
        panel.add(lblKeyPath, gbc);

        // Nút Tạo Chữ Ký 
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.CENTER;
        JButton btnSign = new JButton("Tạo Chữ Ký");
        btnSign.setPreferredSize(new Dimension(150, 35));
        panel.add(btnSign, gbc);

        // Kết quả chữ ký (Ô TextArea)
        gbc.gridy = 4; 
        gbc.fill = GridBagConstraints.BOTH; 
        gbc.weighty = 1.0;
        JTextArea txtSignature = new JTextArea();
        txtSignature.setLineWrap(true);
        txtSignature.setWrapStyleWord(true);
        txtSignature.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(txtSignature);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Kết quả Chữ Ký Điện Tử"));
        panel.add(scrollPane, gbc);

        // Btn Copy 
        gbc.gridy = 5; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.weighty = 0; 
        gbc.anchor = GridBagConstraints.EAST;
        JButton btnCopySign = new JButton("Copy Chữ Ký");
        panel.add(btnCopySign, gbc);

        final File[] privateKeyFile = {null};

        // Sự kiện chọn file
        btnChooseKey.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                privateKeyFile[0] = chooser.getSelectedFile();
                lblKeyPath.setText(privateKeyFile[0].getName());
                lblKeyPath.setForeground(Color.BLUE);
            }
        });

        // Sự kiện Ký
        btnSign.addActionListener(e -> {
            try {
                String orderData = txtOrderInput.getText().trim();
                if (orderData.isEmpty() || privateKeyFile[0] == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập thông tin đơn hàng và chọn file Private Key!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Gọi hàm đã được update
                String signature = SecurityUtil.signData(orderData, privateKeyFile[0]);
                txtSignature.setText(signature);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Khóa không hợp lệ hoặc lỗi xử lý!\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Sự kiện Copy chữ ký
        btnCopySign.addActionListener(e -> copyToClipboard(txtSignature.getText()));

        return panel;
    }

    // phương thức copy
    private void copyToClipboard(String text) {
        if (text != null && !text.isEmpty()) {
            StringSelection stringSelection = new StringSelection(text);
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, null);
            JOptionPane.showMessageDialog(this, "Đã copy vào Clipboard!");
        }
    }
}