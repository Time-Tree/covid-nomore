import React, { useState } from 'react';
import { Text, StyleSheet } from 'react-native';
import LinearGradient from 'react-native-linear-gradient';
import {
  ScrollableScreenShell,
  RoundedCard,
  RoundedButton,
  ScreenHeader
} from '../../components';

import AnswerInput from './AnswerInput';
import AnswerSelect from './AnswerSelect';

const questions = [
  {
    category: 'profile',
    question: 'What is your age',
    type: 'input',
    keyboardType: 'number-pad',
    regex: /^[1-9]\d{0,2}$/,
    exampleValue: 23
  },
  {
    category: 'work',
    question: 'Where do you work?',
    type: 'select',
    options: [
      'Health services where I work with patients',
      'Other services than health services, but with exposure to 10 or more people daily (office, school, business)',
      'Other services than health services with patients, but with exposure to 2-10 people daily',
      "I don't work at the moment or I work alone."
    ]
  },
  {
    category: 'profile',
    question:
      'Do have any of the following symptoms (different from your usual health situation) ?',
    type: 'multiple-select',
    options: [
      'dry cough',
      'wet cough (mucus comes up with cough or can hear mucus sound in lungs)',
      'difficult breathing',
      'chest pain or chest tightness',
      'loss of smell or taste',
      'sore throat or nasal congestion',
      'hoarse voice',
      'body aches'
    ]
  }
];

const Questionnaire = ({ navigation }) => {
  const [questionIndex, setQuestionIndex] = useState(0);
  const question = questions[questionIndex];

  const answerChanged = answer => console.log('~~~', answer);

  const answerControl =
    question.type === 'input' ? (
      <AnswerInput question={question} onAnswerChange={answerChanged} />
    ) : (
      <AnswerSelect question={question} onAnswerChange={answerChanged} />
    );

  return (
    <>
      <ScrollableScreenShell noPadding>
        <ScreenHeader
          title="Covid-19 Form"
          back={() => navigation.navigate('Chat')}
        />
        <RoundedCard style={styles.card}>
          <Text style={styles.questionNumber}>
            Question: {questionIndex + 1}/{questions.length}.{' '}
            {question.category}
          </Text>
          <Text style={styles.question}>{question.question}</Text>

          {answerControl}

          <LinearGradient
            start={{ x: 1, y: 1 }}
            end={{ x: 0, y: 0 }}
            colors={['#0060a8', '#00c0ff']}
            style={styles.footer}
          >
            <RoundedButton
              title="BACK"
              containerStyle={{ ...styles.btnContainer, marginRight: 3.5 }}
              buttonStyle={{ backgroundColor: '#fff' }}
              titleStyle={{ color: '#2c314c' }}
              disabled={questionIndex < 1}
              onPress={() => setQuestionIndex(questionIndex - 1)}
            />
            <RoundedButton
              title="NEXT"
              containerStyle={{ ...styles.btnContainer, marginLeft: 3.5 }}
              buttonStyle={{ backgroundColor: '#fff', opacity: 0.87 }}
              titleStyle={{ color: '#2c314c', opacity: 0.77 }}
              disabled={questionIndex >= questions.length - 1}
              onPress={() => setQuestionIndex(questionIndex + 1)}
            />
          </LinearGradient>
        </RoundedCard>
      </ScrollableScreenShell>
    </>
  );
};

const styles = StyleSheet.create({
  card: {
    paddingTop: 24,
    paddingBottom: 80 + 24,
    paddingHorizontal: 16,
    borderBottomLeftRadius: 0,
    borderBottomRightRadius: 0,
    flex: 1,
    position: 'relative'
  },
  questionNumber: {
    color: '#00c0ff',
    letterSpacing: 0.82,
    textTransform: 'uppercase'
  },
  question: {
    textAlign: 'center',
    fontWeight: 'bold',
    fontSize: 20,
    lineHeight: 28,
    marginVertical: 16
  },
  footer: {
    height: 80,
    borderTopRightRadius: 36,
    borderTopLeftRadius: 36,
    padding: 16,
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    display: 'flex',
    flexDirection: 'row'
  },
  btnContainer: {
    width: 'auto',
    flexGrow: 1
  }
});

export default Questionnaire;
