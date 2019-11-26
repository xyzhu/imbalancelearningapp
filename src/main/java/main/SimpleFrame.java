package main;

import classification.Classification;
import dataprocess.DataPreprocessor;
import org.apache.log4j.Logger;
import util.*;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.List;


public class SimpleFrame extends JFrame{

    private static Logger logger = Logger.getLogger(SimpleFrame.class);

    private JFrame frame;
    private JButton submit, clear;
    private JTextField numFolds_text, times_text;
    private JPanel panel;

    private String project;
    private String filePath;
    private String preFilePath;
    private String classifier_name;
    private String sampling_name;
    private String ensemble_name;
    private int numFolds;
    private int times;
    private List<List<String>> results;
    private Instances data;
    private int tableRows;

    public SimpleFrame() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenHeight = screenSize.height;
        int screenWidth = screenSize.width;

        panel = new JPanel();

        frame = new JFrame();

        frame.setSize(screenWidth / 2, screenHeight / 2);
        frame.setLocationByPlatform(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        frame.add(panel);

        panel.setLayout(null);

        // 选择数据集文件
        JLabel dataset_name_label = new JLabel("数据集：");
        dataset_name_label.setBounds(10, 20, 100, 20);
        panel.add(dataset_name_label);

        preFilePath = filePath = "";
        // 文件名
        JTextArea dialog = new JTextArea();
        dialog.setBounds(110, 20, 165, 20);
        dialog.setEditable(false);
        //dialog.setLineWrap(true);
        panel.add(dialog);

        JLabel errorLabel1 = new JLabel("请选择一个数据集");
        errorLabel1.setBounds(360, 20, 350, 20);
        errorLabel1.setForeground(Color.RED);
        errorLabel1.setVisible(false);
        panel.add(errorLabel1);

        // 文件打开按钮
        JButton openBtn = new JButton("打开");
        openBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showFileOpenDialog(frame, dialog, errorLabel1);
            }
        });
        openBtn.setBounds(280, 20, 60, 20);
        panel.add(openBtn);

        JLabel cr_label = new JLabel("交叉验证：");
        cr_label.setBounds(10, 50, 100, 20);
        panel.add(cr_label);

        JLabel times_label = new JLabel("N次:");
        times_label.setBounds(110, 50, 30, 20);
        panel.add(times_label);

        //times_text
        times_text = new JTextField(60);
        times_text.setText("100");
        times_text.setBounds(140, 50, 50, 20);
        panel.add(times_text);

        JLabel numFolds_label = new JLabel("K折:");
        numFolds_label.setBounds(200, 50, 30, 20);
        panel.add(numFolds_label);


        //numFolds_text
        numFolds_text = new JTextField(60);
        numFolds_text.setText("10");
        numFolds_text.setBounds(230, 50, 50, 20);
        panel.add(numFolds_text);

        JLabel errorLabel2 = new JLabel("输入N不合法");
        errorLabel2.setBounds(280, 50, 150, 20);
        errorLabel2.setForeground(Color.RED);
        errorLabel2.setVisible(false);
        panel.add(errorLabel2);


        // Classifier
        JLabel classifier_label = new JLabel("Classifier:");
        classifier_label.setBounds(10, 80, 100, 20);
        panel.add(classifier_label);

        classifier_name = "j48";
        String[] classifier_list = new String[]{"j48", "naivebayes", "smo", "randomforest", "ripper", "IBk", "LR"};
        JComboBox<String> comboBox = new JComboBox<>(classifier_list);

        // 添加classifier选中状态改变的监听器
        comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // 只处理选中的状态
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    logger.info("Classifier 选中: " + comboBox.getSelectedItem());
                    classifier_name = (String)comboBox.getSelectedItem();
                    PropertyUtil.CUR_DETAIL_FILENAME = PropertyUtil.DETAIL_FOLDER_PATH +
                            PropertyUtil.FILE_PATH_DELIMITER + classifier_name + "_" + project + "_" + "DETAIL";
                }
            }
        });

        // 设置默认选中的条目
        comboBox.setSelectedIndex(0);
        comboBox.setBounds(110, 80, 165, 20);
        panel.add(comboBox);


        // Sampling
        JLabel sampling_label = new JLabel("Sampling:");
        sampling_label.setBounds(10, 110, 100, 20);
        panel.add(sampling_label);

        sampling_name = "Simple";
        String[] sampling_list = new String[]{"Simple", "ROS", "RUS", "Smote"};
        JComboBox<String> sampling_comboBox = new JComboBox<>(sampling_list);

        // 添加sampling选中状态改变的监听器
        sampling_comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // 只处理选中的状态
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    logger.info("Sampling 选中: " + sampling_comboBox.getSelectedItem());
                    sampling_name = (String)sampling_comboBox.getSelectedItem();
                }
            }
        });

        // 设置sampling默认选中的条目
        sampling_comboBox.setSelectedIndex(0);
        sampling_comboBox.setBounds(110, 110, 165, 20);
        panel.add(sampling_comboBox);

        // Ensemble
        JLabel ensemble_label = new JLabel("Ensemble:");
        ensemble_label.setBounds(10, 140, 100, 20);
        panel.add(ensemble_label);

        ensemble_name = "No ensemble";
        String[] ensemble_list = new String[]{"Bagging", "Boosting", "No ensemble"};
        JComboBox<String> ensemble_comboBox = new JComboBox<>(ensemble_list);

        // 添加ensemble选中状态改变的监听器
        ensemble_comboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                // 只处理选中的状态
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    logger.info("Ensemble 选中: " + ensemble_comboBox.getSelectedItem());
                    ensemble_name = (String)ensemble_comboBox.getSelectedItem();
                }
            }
        });

        // 设置 Ensemble 默认选中的条目
        ensemble_comboBox.setSelectedIndex(2);
        ensemble_comboBox.setBounds(110, 140, 165, 20);
        panel.add(ensemble_comboBox);

        // 确定button
        submit = new JButton("确定");
        submit.setBounds(10, 170, 80, 20);
        panel.add(submit);

        // 清空结果button
        clear = new JButton("清空结果");
        clear.setBounds(110, 170, 80, 20);
        panel.add(clear);

        // 结果详情：
        JLabel resultLabel = new JLabel("结果详情");
        resultLabel.setBounds(200, 170, 500, 20);
        resultLabel.setForeground(Color.RED);
        resultLabel.setVisible(false);
        panel.add(resultLabel);


        String[] titles = {"project", "method", "recall-1", "precision-1", "fMeasure-1", "auc"};
        results = new ArrayList<>();
        // 设置初始table大小
        tableRows = 12;
        DefaultTableModel model = new DefaultTableModel(new String[tableRows][titles.length], titles);
        for(int j = 0; j < titles.length; j++) {
            model.setValueAt(titles[j], 0, j);
        }
        JTable table = new JTable(model);
        table.setBounds(10, 200, screenWidth / 2 - 20, screenHeight / 4 - 20);
        //components.add(table);
        panel.add(table);


        // 初始化设置
        logger.info("Resample ratio = " + PropertyUtil.SAMPLE_RATIO);
        FileUtil.checkFolder();


        //注册事件监听和处理事件
        myEvent(panel, errorLabel1, errorLabel2, table, submit, clear, resultLabel);


        //显示窗体
        frame.setVisible(true);

    }

    private void showFileOpenDialog(Component parent, JTextArea dialog, JLabel errLabel) {
        errLabel.setVisible(false);
        errLabel.setText("请选择一个数据集");

        JFileChooser chooser = new JFileChooser();
        String defaultDirectory = "Arffs_old_paper/";


        // 设置默认文件夹
        chooser.setCurrentDirectory(new File(defaultDirectory));

        // 设置默认文件名
        //chooser.setSelectedFile(new File(filePath));

        FileNameExtensionFilter filter = new FileNameExtensionFilter("arff", "arff");
        chooser.setFileFilter(filter);


        int returnVal = chooser.showOpenDialog(parent);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            filePath = file.getAbsolutePath();
            project = file.getName().split(".arf")[0];
            dialog.setText(file.getName());
        }
        if(filePath == null || filePath.length() == 0) {
            errLabel.setVisible(true);
        } else {
            try {
                numFolds = Integer.parseInt(numFolds_text.getText());
                times = Integer.parseInt(times_text.getText());
            } catch (NumberFormatException errsmg) {
                return ;
            }
            if(numFolds <= 0 || times <= 0) return ;
            logger.info("Arff Path is :" + filePath);
        }
    }


    //事件的监听和处理
    private void myEvent(JPanel components, JLabel errorLabel1, JLabel errorLabel2, JTable tabel, JButton submit, JButton clear, JLabel resultLabel){
        //为窗体添加关闭窗口关闭动作的监听
        frame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e){
                System.exit(0);
            }
        });

        // 获取k折
        numFolds_text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                errorLabel2.setVisible(false);
                try {
                    numFolds = Integer.parseInt(numFolds_text.getText());
                } catch(NumberFormatException errsmg) {
                    errorLabel2.setVisible(true);
                    return;
                }
                if(numFolds < 2) {
                    errorLabel2.setText("输入K不合法");
                    errorLabel2.setVisible(true);
                }
                return;
            }
        });

        //获取N次
        times_text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                super.keyReleased(e);
                errorLabel2.setVisible(false);
                try {
                    times = Integer.parseInt(times_text.getText());
                } catch(NumberFormatException errsmg) {
                    errorLabel2.setVisible(true);
                    return;
                }
                if(times <= 0) {
                    errorLabel2.setText("输入N不合法");
                    errorLabel2.setVisible(true);
                }
                return;
            }
        });

        // 点击确定
        submit.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(filePath == null || filePath.length() == 0) {
                    errorLabel1.setText("请选择一个数据集");
                    errorLabel1.setVisible(true);
                } else {
                    try {
                        times = Integer.parseInt(times_text.getText());
                    } catch (NumberFormatException errmsg) {
                        errorLabel2.setText("输入N不合法");
                        errorLabel2.setVisible(true);
                        logger.error(errmsg.getMessage());
                        return;
                    }
                    if(times <= 0) {
                        errorLabel2.setText("输入N不合法");
                        errorLabel2.setVisible(true);
                        return;
                    }
                    try {
                        numFolds = Integer.parseInt(numFolds_text.getText());
                    } catch(NumberFormatException errmsg) {
                        errorLabel2.setText("输入K不合法");
                        errorLabel2.setVisible(true);
                        logger.error(errmsg.getMessage());
                        return;
                    }
                    if(numFolds < 2) {
                        errorLabel2.setText("输入K不合法");
                        errorLabel2.setVisible(true);
                        return;
                    }


                    String[] predict_result;
                    try {
                        if(!preFilePath.equals(filePath)) {
                            preFilePath = filePath;
                            FileReader fr = new FileReader(filePath);
                            BufferedReader br = new BufferedReader(fr);
                            data = new Instances(br);
                            data.setClassIndex(data.numAttributes()-1);
                            //print out number of instances
                            logger.info("Total number of instances: "+data.numInstances());
                            AttributeStats as = data.attributeStats(data.numAttributes()-1);
                            int count[] = as.nominalCounts;
                            logger.info("Number of good instances: " + count[0]);
                            logger.info("Number of buggy instances: " + count[1]);

                            initProperty(classifier_name);
                        }

                        if(results.size()+1 == tableRows) {
                            resultLabel.setText("容量已满，请清空结果");
                            resultLabel.setVisible(true);
                            return;
                        }
                        Classification classification = new Classification(data);
                        predict_result = getClassificationResult();
                    } catch(Exception err) {
                        logger.error("err.getMessage():" + err.getMessage());
                        return;
                    }

                    DefaultTableModel model = (DefaultTableModel) tabel.getModel();
                    List<String> result_list = new ArrayList<>();
                    for(int i = 0; i < predict_result.length; i++) {
                        result_list.add(predict_result[i]);
                    }
                    results.add(result_list);
                    for(int i = 1; i <= results.size(); i++) {
                        List<String> tempList = results.get(i-1);
                        for (int j = 0; j < tempList.size(); j++) {
                            model.setValueAt(tempList.get(j), i, j);
                        }
                    }
                    resultLabel.setText("结果详情请看：" + PropertyUtil.CUR_DETAIL_FILENAME);
                    resultLabel.setVisible(true);
                }
            }
        });

        // 清空结果
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resultLabel.setVisible(false);
                DefaultTableModel model = (DefaultTableModel) tabel.getModel();
                for(int i = 1; i <= results.size(); i++) {
                    List<String> tempList = results.get(i-1);
                    for (int j = 0; j < tempList.size(); j++) {
                        model.setValueAt("", i, j);
                    }
                }
                results = new ArrayList<>();
            }
        });

    }

    private void initProperty(String classifier_name) throws Exception {
        PropertyUtil.CUR_DETAIL_FILENAME = PropertyUtil.DETAIL_FOLDER_PATH +
                PropertyUtil.FILE_PATH_DELIMITER + classifier_name + "_" + project + "_" + "DETAIL";
        File cur_detail_file = new File(PropertyUtil.CUR_DETAIL_FILENAME);
        cur_detail_file.delete();
        cur_detail_file.createNewFile();
        logger.info(project);
        logger.info(classifier_name + " for detail");
    }


    private String[] getClassificationResult() throws Exception{
        String[] predict_result;
        Classification classification = new Classification(data);
        String temp = project + classification.predict(project, classifier_name, sampling_name, ensemble_name, times, numFolds);
        predict_result = temp.split(",");
        return predict_result;
    }


}

