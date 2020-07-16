import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  KeyboardAvoidingView,
  Keyboard,
  ActivityIndicator
} from 'react-native';
import MaterialCommunityIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import reduxContainer from '../redux/reduxContainer';
import NavbarComponent from './components/NavbarComponent';
import tokenActions from '../redux/tokens/actions';
import settingsActions from '../redux/settings/actions';

const ReportMeContainer = props => {
  const [code, setCode] = useState('');
  const [sent, setSent] = useState(false);
  const [easterEgg, setEasterEgg] = useState(0);

  const easterEggHandler = () => {
    if (easterEgg < 12) {
      setEasterEgg(easterEgg + 1);
    } else {
      props.setEasterEggAction(true);
      setEasterEgg(0);
    }
  };

  const sendHandler = () => {
    if (code) {
      props.changeStatusAction(1);
      props.sendTokensAction();
      setSent(true);
      Keyboard.dismiss();
    }
  };

  return (
    <>
      <NavbarComponent title="Report me" />
      <KeyboardAvoidingView behavior="position" keyboardVerticalOffset={20}>
        <View style={styles.content}>
          <Text style={styles.headerText}>
            Has you COVID-19 test come back positive?
          </Text>
          <Text style={styles.text}>
            You have received a code with your test results. Please enter the
            code so that the users you have met can be alerted.
          </Text>
          <TouchableOpacity activeOpacity={1} onPress={easterEggHandler}>
            <Text style={styles.text}>Take good care of yourself!</Text>
          </TouchableOpacity>
        </View>
        <View style={styles.inputContainer}>
          <TextInput
            value={code}
            onChange={setCode}
            style={styles.input}
            placeholder="CODE"
            placeholderTextColor="lightblue"
            onSubmitEditing={sendHandler}
            returnKeyType="done"
            selectionColor="darkblue"
          />
          {props.pending ? (
            <View style={styles.sendButton}>
              <ActivityIndicator color="darkblue" />
            </View>
          ) : (
            <TouchableOpacity style={styles.sendButton} onPress={sendHandler}>
              <MaterialCommunityIcon name="send" size={20} color="darkblue" />
            </TouchableOpacity>
          )}
        </View>
        {sent && !props.pending && (
          <>
            <Text style={styles.sentText}>
              Your code was sent successfully.
            </Text>
            <Text style={styles.sentText}>Thank you!</Text>
          </>
        )}
      </KeyboardAvoidingView>
    </>
  );
};

const styles = StyleSheet.create({
  content: {
    paddingVertical: 50
  },
  headerText: {
    fontWeight: 'bold',
    marginHorizontal: 50,
    paddingVertical: 25,
    textAlign: 'center'
  },
  text: {
    marginHorizontal: 50,
    paddingVertical: 10,
    textAlign: 'center'
  },
  inputContainer: {
    flexDirection: 'row',
    marginTop: 25,
    marginHorizontal: 15,
    backgroundColor: 'white',
    borderRadius: 15,
    borderColor: 'lightgray',
    borderWidth: 1
  },
  sendButton: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    height: 50,
    borderLeftColor: 'lightgray',
    borderLeftWidth: 0.5
  },
  input: {
    flex: 4,
    height: 50,
    color: 'darkblue',
    textAlign: 'center',
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 2
    },
    shadowOpacity: 0.25,
    shadowRadius: 3.84,
    elevation: 5
  },
  sentText: {
    color: 'green',
    textAlign: 'center',
    marginTop: 5
  }
});

function mapStateToProps(state) {
  return {
    pending: state.tokens.send_tokens_pending
  };
}

const dispatchToProps = {
  sendTokensAction: tokenActions.sendTokensAction,
  changeStatusAction: settingsActions.changeStatusAction,
  setEasterEggAction: settingsActions.setEasterEggAction
};

export default reduxContainer(
  ReportMeContainer,
  mapStateToProps,
  dispatchToProps
);
