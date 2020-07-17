import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  TouchableOpacity,
  KeyboardAvoidingView,
  Keyboard,
  ActivityIndicator,
  TouchableWithoutFeedback
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
      if (code === '4321') {
        props.changeStatusAction(1);
        props.sendTokensAction();
      }
      setSent(true);
      Keyboard.dismiss();
    }
  };

  return (
    <>
      <NavbarComponent title="Report me" />
      <TouchableWithoutFeedback onPress={Keyboard.dismiss}>
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
              onChangeText={setCode}
              style={styles.input}
              placeholder="CODE"
              placeholderTextColor="lightblue"
              selectionColor="darkblue"
              keyboardType="numeric"
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
          {sent && !props.pending && !props.error && (
            <>
              <Text style={styles.sentText}>
                Your code was sent successfully.
              </Text>
              <Text style={styles.sentText}>Thank you!</Text>
            </>
          )}
          {sent && !props.pending && props.error && (
            <Text style={styles.errorText}>
              An error occurred, please try again later.
            </Text>
          )}
        </KeyboardAvoidingView>
      </TouchableWithoutFeedback>
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
    marginHorizontal: 15,
    backgroundColor: 'white',
    borderRadius: 15,
    borderColor: 'lightgray',
    borderWidth: 1,
    shadowColor: '#000',
    shadowOffset: {
      width: 0,
      height: 1
    },
    shadowOpacity: 0.15,
    shadowRadius: 3.84,
    elevation: 3
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
    textAlign: 'center'
  },
  sentText: {
    color: 'green',
    textAlign: 'center',
    marginTop: 5
  },
  errorText: {
    color: '#b81d1d',
    textAlign: 'center',
    marginTop: 5
  }
});

function mapStateToProps(state) {
  return {
    pending: state.tokens.send_tokens_pending,
    error: state.tokens.send_tokens_error
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
