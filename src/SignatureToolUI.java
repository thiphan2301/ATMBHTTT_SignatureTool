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
        tabbedPane.addTab("Băm Đơn Hàng", createHashPanel());
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

    // View view cho chức năng băm (hash)
    private JPanel createHashPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Label và ô input
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.add(new JLabel("Nhập chuỗi thông tin đơn hàng cần băm:"), BorderLayout.NORTH);
        
        JTextArea txtInput = new JTextArea();
        txtInput.setLineWrap(true);
        txtInput.setWrapStyleWord(true);
        topPanel.add(new JScrollPane(txtInput), BorderLayout.CENTER);
        panel.add(topPanel, BorderLayout.CENTER); 

        // Nút băm và kết quả
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnHash = new JButton("Băm Dữ Liệu ");
        btnHash.setPreferredSize(new Dimension(200, 35));
        btnPanel.add(btnHash);
        bottomPanel.add(btnPanel, BorderLayout.NORTH);

        // Panel hiển thị kết quả
        JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
        resultPanel.add(new JLabel("Mã băm kết quả:"), BorderLayout.WEST);
        JTextField txtResult = new JTextField();
        txtResult.setEditable(false);
        JButton btnCopy = new JButton("Copy Mã Băm");
        
        resultPanel.add(txtResult, BorderLayout.CENTER);
        resultPanel.add(btnCopy, BorderLayout.EAST);
        
        bottomPanel.add(resultPanel, BorderLayout.SOUTH);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Sự kiện Băm
        btnHash.addActionListener(e -> {
            try {
                String input = txtInput.getText().trim();
                if (input.isEmpty() || input == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập chuỗi đơn hàng!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String hash = SecurityUtil.hashOrderData(input);
                txtResult.setText(hash);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        });

        // Sự kiện Copy
        btnCopy.addActionListener(e -> copyToClipboard(txtResult.getText()));

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

        // Label Mã băm
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Dán Mã Băm (Hash) vào đây:"), gbc);

        // Ô nhập Mã băm
        gbc.gridy = 1;
        JTextField txtHashInput = new JTextField();
        panel.add(txtHashInput, gbc);

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
                String hashValue = txtHashInput.getText().trim();
                if (hashValue.isEmpty() || privateKeyFile[0] == null) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập mã băm và chọn file Private Key!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                String signature = SecurityUtil.signHash(hashValue, privateKeyFile[0]);
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