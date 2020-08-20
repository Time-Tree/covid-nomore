import React, { useState, useEffect } from 'react';
import { StyleSheet } from 'react-native';
import { CheckBox } from 'react-native-elements';

const AnswerSelect = ({ question, onAnswerChange }) => {
  const [selectedOptions, setSelectedOptions] = useState([]);
  useEffect(() => setSelectedOptions([]), [question]);

  const onPress = optionIndex => {
    if (question.type === 'select') {
      setSelectedOptions([optionIndex]);
    } else {
      const selectedIndex = selectedOptions.indexOf(optionIndex);

      if (selectedIndex === -1) {
        setSelectedOptions([...selectedOptions, optionIndex]);
      } else {
        const copy = [...selectedOptions];
        copy.splice(selectedIndex, 1);
        setSelectedOptions(copy);
      }
    }
  };

  const uncheckedIcon = question.type === 'select' ? 'circle-o' : 'square-o';
  const checkedIcon =
    question.type === 'select' ? 'dot-circle-o' : 'check-square';

  return (
    <>
      {question.options.map((option, index) => (
        <CheckBox
          title={option}
          uncheckedIcon={uncheckedIcon}
          checkedIcon={checkedIcon}
          checkedColor="#098DD5"
          uncheckedColor="#B1BAC3"
          containerStyle={styles.checkboxContainer}
          textStyle={styles.checkboxText}
          checked={selectedOptions.includes(index)}
          onPress={() => onPress(index)}
        />
      ))}
    </>
  );
};

const styles = StyleSheet.create({
  checkboxContainer: {
    backgroundColor: 'transparent',
    borderWidth: 0,
    marginLeft: 0,
    marginRight: 0,
    margin: 0,
    paddingVertical: 16,
    paddingHorizontal: 0,
    borderBottomColor: '#F4F4F4',
    borderBottomWidth: 1
  },
  checkboxText: {
    fontWeight: 'normal',
    color: '#333333',
    fontSize: 17,
    flex: 1,
    flexWrap: 'wrap'
  }
});

export default AnswerSelect;
