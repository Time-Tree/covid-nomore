import React from 'react';
import { Text, StyleSheet } from 'react-native';
import {
  ScrollableScreenShell,
  RoundedCard,
  ScreenHeader
} from '../../components';

const Results = ({ navigation }) => {
  const results = ['negative', 'positive', null];
  const result = results[Math.floor(Math.random() * results.length)]; // random result

  const resultColor = result === 'negative' ? '#00c0ff' : '#ff436d';
  const resultBadgeStyle = {
    ...styles.result,
    color: resultColor,
    borderColor: resultColor
  };

  return (
    <ScrollableScreenShell noPadding>
      <ScreenHeader title="My result" back={() => navigation.goBack()} />
      <RoundedCard style={styles.card}>
        <Text style={styles.header}>Your result:</Text>
        {result ? (
          <Text style={resultBadgeStyle}>{result}</Text>
        ) : (
          <Text style={styles.notFound}>Data not found</Text>
        )}
      </RoundedCard>

      {result && (
        <RoundedCard style={styles.card}>
          <Text style={styles.header}>What should I do?</Text>
          <Text style={styles.text}>
            Your healthcare provider, your national public health authority and
            and your employer are all potential sources of accurate information
            information on COVID-19 and whether it is in your area. It is
            important to be informed of the situation where you live and take
            appropriate measures to protect yourself.{'\n'}If you are in an area
            area where there is an outbreak of COVID-19 you need to take the
            risk of infection seriously. Follow the advice issued by national
            and local health authorities.You can ask any questions to healthcare
            healthcare provider in the
            <Text
              style={{ ...styles.link, ...styles.text }}
              onPress={() => navigation.navigate('Chat')}
            >
              {' '}
              chat
            </Text>
            .
          </Text>
        </RoundedCard>
      )}
    </ScrollableScreenShell>
  );
};

const styles = StyleSheet.create({
  card: {
    minHeight: 181,
    paddingVertical: 24,
    paddingHorizontal: 16,
    marginBottom: 16,
    marginHorizontal: 16
  },
  header: {
    fontSize: 34,
    lineHeight: 41,
    color: '#2c314c',
    textAlign: 'center'
  },
  notFound: {
    textAlign: 'center',
    marginTop: 36,
    color: '#9ba2ab',
    fontSize: 28,
    lineHeight: 32
  },
  result: {
    marginTop: 24,
    width: 150,
    marginLeft: 'auto',
    marginRight: 'auto',
    textAlign: 'center',
    textTransform: 'capitalize',
    fontSize: 28,
    lineHeight: 32,
    paddingTop: 12,
    paddingBottom: 8,
    borderRadius: 8,
    borderWidth: 2
  },
  text: {
    marginTop: 16,
    fontSize: 14,
    lineHeight: 20
  },
  link: {
    color: '#008bcf'
  }
});

export default Results;
