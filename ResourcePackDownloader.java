// Assuming you have a main UI class
public class MainUI extends JFrame {
    private JButton downloadButton;
    private JTextField urlField;

    public MainUI() {
        // Your existing UI setup code

        // Add the new components
        urlField = new JTextField(25);
        downloadButton = new JButton("Download Resource Pack");

        downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String url = urlField.getText();
                downloadResourcePack(url);
            }
        });

        // Add components to your layout
        JPanel panel = new JPanel();
        panel.add(new JLabel("Resource Pack URL:"));
        panel.add(urlField);
        panel.add(downloadButton);

        add(panel, BorderLayout.SOUTH); // Adjust layout as needed
    }

    private void downloadResourcePack(String urlString) {
        try (BufferedInputStream in = new BufferedInputStream(new URL(urlString).openStream());
             FileOutputStream fileOutputStream = new FileOutputStream("resourcepacks/pack.zip")) {
            byte dataBuffer[] = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                fileOutputStream.write(dataBuffer, 0, bytesRead);
            }
            JOptionPane.showMessageDialog(this, "Download Complete!");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Download Failed: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainUI().setVisible(true);
            }
        });
    }
}
