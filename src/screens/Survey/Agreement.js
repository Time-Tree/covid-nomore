import React, { useState } from 'react';
import { Text, StyleSheet, View } from 'react-native';
import { CheckBox } from 'react-native-elements';
import { BasicScreenShell, RoundedCard, RoundedButton } from '../../components';

const Main = ({ navigation }) => {
  const [userAgrees, setUserAgrees] = useState(false);

  return (
    <BasicScreenShell>
      <RoundedCard style={styles.card}>
        <Text style={styles.header}>Covid-19 Form</Text>
        <Text style={styles.text}>
          If you experience any of the symptoms below, please call Emergency
          Paramedics{'\n'}- Constant severe chest pain or pressure{'\n'}-
          Extreme difficulty breathing
        </Text>
        <Text style={styles.text}>
          DON'T SHARE ANY PERSONALLY identifiable information like email, phone
          number, etc. All fields are optional, don't share anything you don't
          feel comfortable in sharing. The more data we collect, the more
          patterns we can see and the faster we can all stop covid-19. All
          results will be posted on{' '}
          <Text style={styles.link}>https://feeel.health/en</Text>
        </Text>
        <Text style={styles.text}>
          I hereby confirm that I am over the age of 18 and I agree that the
          information provided to be processed (including by automated methods)
          and used in any projects related to covid-19. *
        </Text>
        <View style={styles.checkboxRow}>
          <CheckBox
            title="I agree"
            checkedIcon="dot-circle-o"
            uncheckedIcon="circle-o"
            checkedColor="#098DD5"
            uncheckedColor="#B1BAC3"
            containerStyle={styles.checkboxContainer}
            textStyle={styles.checkboxText}
            checked={userAgrees}
            onPress={() => setUserAgrees(true)}
          />
          <CheckBox
            title="I don't agree"
            checkedIcon="dot-circle-o"
            uncheckedIcon="circle-o"
            checkedColor="#ff436d"
            uncheckedColor="#B1BAC3"
            containerStyle={styles.checkboxContainer}
            textStyle={styles.checkboxText}
            checked={!userAgrees}
            onPress={() => setUserAgrees(false)}
          />
        </View>
        <RoundedButton
          title="START SURVEY"
          disabled={!userAgrees}
          containerStyle={styles.btnContainer}
          buttonStyle={styles.btn}
          onPress={() => {}}
        />
      </RoundedCard>
    </BasicScreenShell>
  );
};

const styles = StyleSheet.create({
  card: {
    paddingVertical: 24,
    paddingHorizontal: 16
  },
  header: {
    fontSize: 34,
    lineHeight: 41,
    color: '#2c314c',
    textAlign: 'center'
  },
  text: {
    marginTop: 16,
    fontSize: 14,
    lineHeight: 20
  },
  link: {
    textDecorationLine: 'underline',
    color: '#008bcf'
  },
  checkboxRow: {
    display: 'flex',
    flexDirection: 'row',
    justifyContent: 'space-between'
  },
  checkboxContainer: {
    backgroundColor: 'transparent',
    borderWidth: 0,
    marginLeft: 0,
    marginRight: 0,
    marginTop: 6,
    paddingHorizontal: 0
  },
  checkboxText: {
    fontWeight: 'normal',
    color: '#333333',
    fontSize: 17
  },
  btnContainer: {
    marginLeft: 'auto',
    marginRight: 'auto',
    marginTop: 24
  }
});

export default Main;
