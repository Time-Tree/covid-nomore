import React from 'react';
import { View, Text, TouchableOpacity, Image, StyleSheet } from 'react-native';

import picFeelBad from './picFeelBad.png';
import picFeelNormal from './picFeelNormal.png';
import picFeelGreat from './picFeelGreat.png';

const FeelingFeedbackSelect = ({ selectedFeedback, onFeedback }) => {
  const defaultEmojiBorder = '#f7f8fc';

  const badEmojiBorder =
    selectedFeedback === 'bad'
      ? 'rgba(255, 67, 109, 0.25)'
      : defaultEmojiBorder;

  const normalEmojiBorder =
    selectedFeedback === 'normal'
      ? 'rgba(222, 172, 0, 0.25)'
      : defaultEmojiBorder;

  const greatEmojiBorder =
    selectedFeedback === 'great'
      ? 'rgba(0, 222, 102, 0.25)'
      : defaultEmojiBorder;

  return (
    <>
      <Text style={styles.header}>How are you feeling today?</Text>
      <View style={styles.feedbackWrapper}>
        <TouchableOpacity
          style={{ ...styles.emojiWrapper, borderColor: badEmojiBorder }}
          onPress={() => onFeedback('bad')}
        >
          <Image source={picFeelBad} style={styles.emoji} />
        </TouchableOpacity>
        <TouchableOpacity
          style={{ ...styles.emojiWrapper, borderColor: normalEmojiBorder }}
          onPress={() => onFeedback('normal')}
        >
          <Image source={picFeelNormal} style={styles.emoji} />
        </TouchableOpacity>
        <TouchableOpacity
          style={{ ...styles.emojiWrapper, borderColor: greatEmojiBorder }}
          onPress={() => onFeedback('great')}
        >
          <Image source={picFeelGreat} style={styles.emoji} />
        </TouchableOpacity>
      </View>
    </>
  );
};

const styles = StyleSheet.create({
  header: {
    textAlign: 'center',
    fontSize: 20,
    fontWeight: 'bold'
  },
  feedbackWrapper: {
    marginTop: 24,
    marginBottom: 24,
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-around'
  },
  emojiWrapper: {
    borderWidth: 12,
    borderRadius: 30
  },
  emoji: {
    width: 36,
    height: 36
  }
});

export default FeelingFeedbackSelect;
