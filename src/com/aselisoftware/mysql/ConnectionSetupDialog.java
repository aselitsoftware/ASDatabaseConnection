package com.aselisoftware.mysql;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class ConnectionSetupDialog extends Dialog {

	protected String result;
	protected Shell shell;
	private Text textHost;
	private Text textLogin;
	private Text textPassword;
	private Text textDatabase;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public ConnectionSetupDialog(Shell parent, int style, String caption) {
		
		super(parent, style);
		setText(caption);
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public String open(String connectionParams) {
		
		createContents();

		textHost.setText(ConnectionInstaller.getHost(connectionParams));
		textLogin.setText(ConnectionInstaller.getLogin(connectionParams));
		textPassword.setText(ConnectionInstaller.getPassword(connectionParams));
		textDatabase.setText(ConnectionInstaller.getDatabase(connectionParams));
		
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}
	
	/**
	 * Make connection parameters string with ";" delimiters.
	 * @return
	 */
	private String makeConnectionParams() {
		
		return "Host: " + textHost.getText() + "; Login: " + textLogin.getText() +
			"; Password: " + textPassword.getText() + "; Database: " +
			textDatabase.getText() + ";";
	}
	
	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		
		shell = new Shell(getParent(), SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setSize(320, 246);
		shell.setText(getText());
		shell.setLayout(null);
		
		Label lblHost = new Label(shell, SWT.NONE);
		lblHost.setBounds(10, 10, 75, 15);
		lblHost.setText("Host:");
		
		textHost = new Text(shell, SWT.BORDER);
		textHost.setBounds(10, 27, 294, 21);
		
		Label lblLogin = new Label(shell, SWT.NONE);
		lblLogin.setBounds(10, 54, 75, 15);
		lblLogin.setText("Login:");
		
		textLogin = new Text(shell, SWT.BORDER);
		textLogin.setBounds(10, 71, 294, 21);
		
		Label lblPassport = new Label(shell, SWT.NONE);
		lblPassport.setText("Password:");
		lblPassport.setBounds(10, 98, 75, 15);
		
		textPassword = new Text(shell, SWT.BORDER);
		textPassword.setBounds(10, 115, 294, 21);
		
		Label lblDatabase = new Label(shell, SWT.NONE);
		lblDatabase.setText("Database:");
		lblDatabase.setBounds(10, 142, 75, 15);
		
		textDatabase = new Text(shell, SWT.BORDER);
		textDatabase.setBounds(10, 159, 294, 21);
		
		Button btnTestConnection = new Button(shell, SWT.NONE);
		btnTestConnection.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				ConnectionInstaller connection = null;
				try {
					try {
						String connectionParams = makeConnectionParams();
						connection = new ConnectionInstaller(connectionParams);
						if (connection.connect()) {
						
							connection.checkDatabase(connectionParams);
							
							MessageBox mb = new MessageBox(shell, SWT.ICON_INFORMATION);
							mb.setText(getParent().getText());
							mb.setMessage("Connection is established successfully.");
							mb.open();
						} else
							throw new Exception("Error occur while setup connection.");
					}
					catch (Exception ex) {
						
						if (!ex.getMessage().isEmpty()) {
							
							MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
							mb.setText(getParent().getText());
							mb.setMessage(ex.getMessage());
							mb.open();
						}
					}
				} finally {
					
					if (null != connection)
						connection.close();
				}
			}
		});
		btnTestConnection.setBounds(10, 186, 132, 25);
		btnTestConnection.setText("Test connection");
		
		Button btnOk = new Button(shell, SWT.NONE);
		btnOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				try {
					ConnectionInstaller.checkParams("", textHost.getText(),
						textLogin.getText(), textPassword.getText(),
						textDatabase.getText());
					result = makeConnectionParams();
					shell.close();
				} catch (Exception ex) {
					
					if (!ex.getMessage().isEmpty()) {
						
						MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR);
						mb.setText(getParent().getText());
						mb.setMessage(ex.getMessage());
						mb.open();
					}
				}
			}
		});
		btnOk.setBounds(148, 186, 75, 25);
		btnOk.setText("OK");
		
		Button btnCancel = new Button(shell, SWT.NONE);
		btnCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			
				result = null;
				shell.close();
			}
		});
		btnCancel.setBounds(229, 186, 75, 25);
		btnCancel.setText("Cancel");
		
		shell.setTabList(new Control[]{textHost, textLogin, textPassword, textDatabase, btnTestConnection, btnOk, btnCancel});
	}
}
