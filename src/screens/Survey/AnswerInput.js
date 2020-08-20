import React, { useState, useEffect } from 'react';
import { Text, StyleSheet, TextInput } from 'react-native';

const AnswerInput = ({ question, onAnswerChange }) => {
  const [inputVal, setInputVal] = useState('');
  const [hasInputError, setHasInputError] = useState(false);

  const onTextInputChange = val => {
    setInputVal(val);

    const isValid = question.regex.test(val);
    setHasInputError(!isValid);

    if (isValid) {
      onAnswerChange(val);
    }
  };

  useEffect(() => {
    setInputVal('');
    setHasInputError(false);
  }, [question]);

  const inputStyles = {
    ...styles.textInput,
    borderBottomColor: hasInputError ? '#E77F7F' : '#F4F4F4'
  };

  return (
    <>
      <TextInput
        style={inputStyles}
        onChangeText={onTextInputChange}
        value={inputVal}
        placeholder="Your answer"
        placeholderTextColor="#7F7F7F"
        contextMenuHidden={true}
        keyboardType={question.keyboardType}
      />
      {hasInputError && (
        <Text style={styles.inputErrorText}>
          * Invalid Value. Correct example: {question.exampleValue}
        </Text>
      )}
    </>
  );
};

const styles = StyleSheet.create({
  textInput: {
    borderBottomColor: '#F4F4F4',
    borderBottomWidth: 1,
    paddingVertical: 18,
    fontSize: 17
  },
  inputErrorText: {
    color: 'red',
    fontSize: 13,
    marginTop: 4
  }
});

export default AnswerInput;
