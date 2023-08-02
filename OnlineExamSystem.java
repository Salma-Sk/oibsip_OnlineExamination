import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

class MCQQuestion {
    private String question;
    private List<String> options;
    private int correctOption;

    public MCQQuestion(String question, List<String> options, int correctOption) {
        this.question = question;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getQuestion() {
        return question;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOption() {
        return correctOption;
    }
}

class User {
    private String username;
    private String password;
    private String name;

    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setName(String name) {
        this.name = name;
    }
}

public class OnlineExamSystem {
    private List<MCQQuestion> questions;
    private int currentQuestionIndex;
    private JFrame frame;
    private JTextArea questionTextArea;
    private ButtonGroup optionGroup;
    private JRadioButton[] optionRadioButtons;
    private JButton nextButton;
    private JLabel timerLabel;
    private JLabel scoreLabel;
    private User currentUser;

    private int timeRemaining;
    private Timer timer;
    private int score;

    public OnlineExamSystem() {
        questions = generateMCQs();
        currentQuestionIndex = 0;
        score = 0;

        frame = new JFrame("Quiz App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        JPanel userPanel = new JPanel();
        JLabel nameLabel = new JLabel("Name:");
        JLabel usernameLabel = new JLabel("Username:");
        JTextField nameField = new JTextField(10);
        JTextField usernameField = new JTextField(10);
        JButton loginButton = new JButton("Login");
        userPanel.add(nameLabel);
        userPanel.add(nameField);
        userPanel.add(usernameLabel);
        userPanel.add(usernameField);
        userPanel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = nameField.getText();
                String username = usernameField.getText();
                if (name.isEmpty() || username.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "Please enter your name and username.");
                } else {
                    currentUser = new User(username, "", name);
                    startQuiz();
                }
            }
        });

        frame.add(userPanel, BorderLayout.NORTH);
        frame.setVisible(true);
    }

    private void startQuiz() {
        showProfilePanel();
        showCurrentQuestion();
        startTimer();
        frame.remove(frame.getContentPane().getComponent(0)); // Remove login panel
    }

    private void showProfilePanel() {
        JPanel profilePanel = new JPanel();
        JLabel nameLabel = new JLabel("Name: " + currentUser.getName());
        JLabel usernameLabel = new JLabel("Username: " + currentUser.getUsername());
        JButton updateButton = new JButton("Update Profile");
        JButton changePasswordButton = new JButton("Change Password");

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newName = JOptionPane.showInputDialog(frame, "Enter new name:");
                if (newName != null && !newName.isEmpty()) {
                    currentUser.setName(newName);
                    nameLabel.setText("Name: " + currentUser.getName());
                }
            }
        });

        changePasswordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newPassword = JOptionPane.showInputDialog(frame, "Enter new password:");
                if (newPassword != null && !newPassword.isEmpty()) {
                    currentUser.setPassword(newPassword);
                }
            }
        });

        profilePanel.add(nameLabel);
        profilePanel.add(usernameLabel);
        profilePanel.add(updateButton);
        profilePanel.add(changePasswordButton);

        frame.add(profilePanel, BorderLayout.NORTH);
        frame.revalidate();
    }

    private void showCurrentQuestion() {
        MCQQuestion question = questions.get(currentQuestionIndex);
        questionTextArea = new JTextArea(question.getQuestion());
        questionTextArea.setEditable(false);
        optionGroup = new ButtonGroup();
        optionRadioButtons = new JRadioButton[4];

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new GridLayout(0, 1));

        for (int i = 0; i < 4; i++) {
            optionRadioButtons[i] = new JRadioButton(question.getOptions().get(i));
            optionGroup.add(optionRadioButtons[i]);
            optionsPanel.add(optionRadioButtons[i]);
        }

        JPanel questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.add(questionTextArea);
        questionPanel.add(optionsPanel);

        nextButton = new JButton("Next");
        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAnswerAndShowNextQuestion();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(nextButton);

        timerLabel = new JLabel("Time Remaining: " + timeRemaining + " seconds");
        scoreLabel = new JLabel("Score: " + score);

        frame.getContentPane().removeAll();
        frame.add(timerLabel, BorderLayout.NORTH);
        frame.add(scoreLabel, BorderLayout.SOUTH);
        frame.add(questionPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.revalidate();

        if (currentQuestionIndex == questions.size() - 1) {
            nextButton.setText("Finish");
        }
    }

    private void checkAnswerAndShowNextQuestion() {
        int selectedOption = getSelectedOption();
        if (selectedOption != -1) {
            MCQQuestion question = questions.get(currentQuestionIndex);
            if (question.getCorrectOption() == selectedOption) {
                JOptionPane.showMessageDialog(frame, "Correct answer!");
                score++;
                scoreLabel.setText("Score: " + score);
            } else {
                JOptionPane.showMessageDialog(frame, "Incorrect answer!");
            }

            currentQuestionIndex++;
            if (currentQuestionIndex < questions.size()) {
                showCurrentQuestion();
                startTimer();
            } else {
                stopTimer();
                displayResult();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select an option.");
        }
    }

    private int getSelectedOption() {
        for (int i = 0; i < optionRadioButtons.length; i++) {
            if (optionRadioButtons[i].isSelected()) {
                return i;
            }
        }
        return -1;
    }

    private void startTimer() {
        timeRemaining = 60; // Set time for 60 seconds
        timerLabel.setText("Time Remaining: " + timeRemaining + " seconds");
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (timeRemaining > 0) {
                    timeRemaining--;
                    timerLabel.setText("Time Remaining: " + timeRemaining + " seconds");
                } else {
                    stopTimer();
                    JOptionPane.showMessageDialog(frame, "Time's up!");
                    checkAnswerAndShowNextQuestion();
                }
            }
        }, 1000, 1000); // Delay 1 second, repeat every 1 second
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
    }

    private List<MCQQuestion> generateMCQs() {
        List<MCQQuestion> questions = new ArrayList<>();
    
        questions.add(new MCQQuestion(
            "Which keyword is used to declare a class in Java?",
            Arrays.asList("A. method", "B. variable", "C. class", "D. interface"),
            2 // Correct: C. class
        ));
    
        questions.add(new MCQQuestion(
            "Which data type is used to represent a single 16-bit Unicode character in Java?",
            Arrays.asList("A. char", "B. string", "C. int", "D. byte"),
            0 // Correct: A. char
        ));
    
        questions.add(new MCQQuestion(
            "What is the result of 7 % 3 in Java?",
            Arrays.asList("A. 2", "B. 1", "C. 0", "D. 3"),
            1 // Correct: B. 1
        ));
    
        questions.add(new MCQQuestion(
            "Which keyword is used to create an instance of a class in Java?",
            Arrays.asList("A. new", "B. instance", "C. create", "D. make"),
            0 // Correct: A. new
        ));
    
        questions.add(new MCQQuestion(
            "What is the default value of an uninitialized boolean variable in Java?",
            Arrays.asList("A. true", "B. false", "C. 0", "D. null"),
            1 // Correct: B. false
        ));
    
        questions.add(new MCQQuestion(
            "Which loop is guaranteed to execute at least once in Java?",
            Arrays.asList("A. for loop", "B. while loop", "C. do-while loop", "D. switch loop"),
            2 // Correct: C. do-while loop
        ));
    
        questions.add(new MCQQuestion(
            "Which of the following is NOT a valid access modifier in Java?",
            Arrays.asList("A. public", "B. private", "C. protected", "D. static"),
            3 // Correct: D. static
        ));
    
        questions.add(new MCQQuestion(
            "What is the parent class of all classes in Java?",
            Arrays.asList("A. Object", "B. Class", "C. Base", "D. Super"),
            0 // Correct: A. Object
        ));
    
        questions.add(new MCQQuestion(
            "Which keyword is used to prevent a class from being subclassed in Java?",
            Arrays.asList("A. final", "B. abstract", "C. locked", "D. sealed"),
            0 // Correct: A. final
        ));
    
        questions.add(new MCQQuestion(
            "What is the purpose of the 'static' keyword in Java?",
            Arrays.asList("A. It indicates that a variable is not modifiable.", "B. It specifies a variable as constant.", "C. It makes a method or variable belong to the class, rather than an instance.", "D. It indicates a variable is read-only."),
            2 // Correct: C. It makes a method or variable belong to the class, rather than an instance.
        ));
    
        return questions;
    }

    private void displayResult() {
        int correctAnswers = score;
        int totalQuestions = questions.size();

        String resultMessage = "Quiz completed!\n";
        resultMessage += "Hello, " + currentUser.getName() + "!\n";
        resultMessage += "Correct Answers: " + correctAnswers + "/" + totalQuestions;

        JOptionPane.showMessageDialog(frame, resultMessage);
        frame.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new OnlineExamSystem();
            }
        });
    }
}
