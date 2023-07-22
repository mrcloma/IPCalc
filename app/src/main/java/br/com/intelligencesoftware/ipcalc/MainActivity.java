package br.com.intelligencesoftware.ipcalc;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText ipPrefixInput;
    private Button calculateButton;
    private TextView resultTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ipPrefixInput = findViewById(R.id.editTextIPPrefix);
        calculateButton = findViewById(R.id.buttonCalculate);
        resultTextView = findViewById(R.id.textViewResult);

        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateNetworkInfo();
            }
        });
    }

    private void calculateNetworkInfo() {
        String input = ipPrefixInput.getText().toString().trim();

        if (TextUtils.isEmpty(input)) {
            resultTextView.setText("Insira um endereço IP com prefixo de rede.");
            return;
        }

        String[] parts = input.split("/");
        if (parts.length != 2) {
            resultTextView.setText("Endereço IP ou prefixo de rede inválido.");
            return;
        }

        String ipAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        if (!isValidIPAddress(ipAddress) || prefixLength < 0 || prefixLength > 32) {
            resultTextView.setText("Endereço IP ou prefixo de rede inválido.");
            return;
        }

        String subnetMask = getSubnetMaskFromPrefix(prefixLength);
        String wildcardMask = getWildcardMask(subnetMask);
        String networkAddress = getNetworkAddress(ipAddress, subnetMask);
        String firstUsableIP = getFirstUsableIPAddress(networkAddress);
        String lastUsableIP = getLastUsableIPAddress(networkAddress, wildcardMask);
        String broadcastIP = getBroadcastAddress(networkAddress, wildcardMask);
        int usableIPs = getUsableIPCount(prefixLength);

        StringBuilder result = new StringBuilder();
        result.append("Máscara de rede: ").append(subnetMask).append("\n");
        result.append("Wildcard mask: ").append(wildcardMask).append("\n");
        result.append("IP da rede: ").append(networkAddress).append("\n");
        result.append("Primeiro IP utilizável: ").append(firstUsableIP).append("\n");
        result.append("Último IP utilizável: ").append(lastUsableIP).append("\n");
        result.append("IP de broadcast: ").append(broadcastIP).append("\n");
        result.append("Quantidade de IPs utilizáveis: ").append(usableIPs);

        resultTextView.setText(result.toString());
    }

    private boolean isValidIPAddress(String ipAddress) {
        String IP_ADDRESS_PATTERN =
                "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
                        + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

        Pattern pattern = Pattern.compile(IP_ADDRESS_PATTERN);
        Matcher matcher = pattern.matcher(ipAddress);

        return matcher.matches();
    }

    private String getSubnetMaskFromPrefix(int prefixLength) {
        int value = 0xffffffff << (32 - prefixLength);
        int octet3 = value & 0xff;
        int octet2 = (value >> 8) & 0xff;
        int octet1 = (value >> 16) & 0xff;
        int octet0 = (value >> 24) & 0xff;

        return octet0 + "." + octet1 + "." + octet2 + "." + octet3;
    }

    private String getWildcardMask(String subnetMask) {
        String[] maskParts = subnetMask.split("\\.");
        int[] wildcardMaskParts = new int[maskParts.length];

        for (int i = 0; i < maskParts.length; i++) {
            int part = Integer.parseInt(maskParts[i]);
            wildcardMaskParts[i] = 255 - part;
        }

        return wildcardMaskParts[0] + "." + wildcardMaskParts[1] + "." + wildcardMaskParts[2] + "." + wildcardMaskParts[3];
    }

    private String getNetworkAddress(String ipAddress, String subnetMask) {
        String[] ipParts = ipAddress.split("\\.");
        String[] maskParts = subnetMask.split("\\.");

        int[] networkAddressParts = new int[ipParts.length];

        for (int i = 0; i < ipParts.length; i++) {
            int ipPart = Integer.parseInt(ipParts[i]);
            int maskPart = Integer.parseInt(maskParts[i]);
            networkAddressParts[i] = ipPart & maskPart;
        }

        return networkAddressParts[0] + "." + networkAddressParts[1] + "." + networkAddressParts[2] + "." + networkAddressParts[3];
    }

    private String getFirstUsableIPAddress(String networkAddress) {
        String[] parts = networkAddress.split("\\.");
        int lastPart = Integer.parseInt(parts[3]) + 1;
        return parts[0] + "." + parts[1] + "." + parts[2] + "." + lastPart;
    }

    private String getLastUsableIPAddress(String networkAddress, String wildcardMask) {
        String[] networkAddressParts = networkAddress.split("\\.");
        String[] wildcardMaskParts = wildcardMask.split("\\.");

        int[] lastUsableIPParts = new int[networkAddressParts.length];

        for (int i = 0; i < networkAddressParts.length; i++) {
            int networkPart = Integer.parseInt(networkAddressParts[i]);
            int wildcardPart = Integer.parseInt(wildcardMaskParts[i]);
            lastUsableIPParts[i] = networkPart | wildcardPart;
        }

        int lastPart = lastUsableIPParts[3] - 1;
        return lastUsableIPParts[0] + "." + lastUsableIPParts[1] + "." + lastUsableIPParts[2] + "." + lastPart;
    }

    private String getBroadcastAddress(String networkAddress, String wildcardMask) {
        String[] networkAddressParts = networkAddress.split("\\.");
        String[] wildcardMaskParts = wildcardMask.split("\\.");

        int[] broadcastIPParts = new int[networkAddressParts.length];

        for (int i = 0; i < networkAddressParts.length; i++) {
            int networkPart = Integer.parseInt(networkAddressParts[i]);
            int wildcardPart = Integer.parseInt(wildcardMaskParts[i]);
            broadcastIPParts[i] = networkPart | wildcardPart;
        }

        return broadcastIPParts[0] + "." + broadcastIPParts[1] + "." + broadcastIPParts[2] + "." + broadcastIPParts[3];
    }

    private int getUsableIPCount(int prefixLength) {
        return (int) Math.pow(2, 32 - prefixLength) - 2;
    }
}

