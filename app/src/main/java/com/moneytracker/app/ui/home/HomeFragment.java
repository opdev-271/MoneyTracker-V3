package com.moneytracker.app.ui.home;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.moneytracker.app.R;
import com.moneytracker.app.data.entity.Account;
import com.moneytracker.app.data.entity.Category;
import com.moneytracker.app.databinding.FragmentHomeBinding;
import com.moneytracker.app.ui.addtransaction.AddTransactionActivity;
import com.moneytracker.app.util.CurrencyFormatter;
import com.moneytracker.app.util.DisplayNameResolver;
import com.moneytracker.app.util.ViewAnimations;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String HIDDEN_BALANCE_TEXT = "••••••";

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private TransactionAdapter adapter;

    private Double latestBalance;
    private double displayedBalance = Double.NaN;
    private ValueAnimator balanceAnimator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        ViewAnimations.fadeInContent(view);

        adapter = new TransactionAdapter();
        binding.recyclerTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerTransactions.setAdapter(adapter);

        binding.btnToggleBalance.setOnClickListener(v -> viewModel.toggleBalanceHidden());
        binding.btnSeeAll.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.see_all, Toast.LENGTH_SHORT).show());

        binding.btnExpense.setOnClickListener(v -> openAddTransaction(Category.TYPE_EXPENSE));
        binding.btnIncome.setOnClickListener(v -> openAddTransaction(Category.TYPE_INCOME));

        binding.textAccountInfo.setOnClickListener(this::showAccountSwitcher);

        ViewAnimations.addPressFeedback(binding.btnExpense, binding.btnIncome, binding.btnSeeAll);

        observeViewModel();
    }

    private void observeViewModel() {
        viewModel.getAccounts().observe(getViewLifecycleOwner(), accounts -> {
            viewModel.ensureDefaultAccountSelected(accounts);
        });

        viewModel.getSelectedAccount().observe(getViewLifecycleOwner(), account -> {
            if (account != null) {
                binding.textAccountInfo.setText(DisplayNameResolver.resolve(requireContext(), account.getName()));
            }
        });

        viewModel.getSelectedAccountBalance().observe(getViewLifecycleOwner(), balance -> {
            latestBalance = balance;
            if (!Boolean.TRUE.equals(viewModel.getBalanceHidden().getValue())) {
                animateBalanceTo(balance);
            }
        });

        viewModel.getBalanceHidden().observe(getViewLifecycleOwner(), hidden -> {
            animateEyeIconSwap(Boolean.TRUE.equals(hidden));
            if (Boolean.TRUE.equals(hidden)) {
                if (balanceAnimator != null) {
                    balanceAnimator.cancel();
                }
                displayedBalance = Double.NaN;
                binding.textBalance.setText(HIDDEN_BALANCE_TEXT);
            } else {
                animateBalanceTo(latestBalance);
            }
        });

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            boolean empty = transactions == null || transactions.isEmpty();
            binding.textEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerTransactions.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    private void animateBalanceTo(Double balance) {
        if (balance == null || binding == null) {
            return;
        }
        String currency = viewModel.getSelectedAccount().getValue() != null
                ? viewModel.getSelectedAccount().getValue().getCurrencyCode() : "CZK";

        double from = Double.isNaN(displayedBalance) ? 0 : displayedBalance;
        displayedBalance = balance;

        if (balanceAnimator != null) {
            balanceAnimator.cancel();
        }
        balanceAnimator = ValueAnimator.ofFloat((float) from, balance.floatValue());
        balanceAnimator.setDuration(700);
        balanceAnimator.setInterpolator(new DecelerateInterpolator());
        balanceAnimator.addUpdateListener(animation ->
                binding.textBalance.setText(CurrencyFormatter.format((float) animation.getAnimatedValue(), currency)));
        balanceAnimator.start();
    }

    private void animateEyeIconSwap(boolean hidden) {
        binding.btnToggleBalance.animate().scaleX(0f).scaleY(0f).setDuration(120)
                .withEndAction(() -> {
                    if (binding == null) {
                        return;
                    }
                    binding.btnToggleBalance.setImageResource(hidden ? R.drawable.ic_eye_off : R.drawable.ic_eye);
                    binding.btnToggleBalance.animate().scaleX(1f).scaleY(1f).setDuration(180)
                            .setInterpolator(new OvershootInterpolator(2.2f)).start();
                }).start();
    }

    private void showAccountSwitcher(View anchor) {
        List<Account> accounts = viewModel.getAccounts().getValue();
        if (accounts == null || accounts.size() < 2) {
            return;
        }
        PopupMenu popupMenu = new PopupMenu(requireContext(), anchor);
        for (Account account : accounts) {
            popupMenu.getMenu().add(0, (int) account.getId(), 0,
                    DisplayNameResolver.resolve(requireContext(), account.getName()));
        }
        popupMenu.setOnMenuItemClickListener(item -> {
            viewModel.selectAccount(item.getItemId());
            return true;
        });
        popupMenu.show();
    }

    private void openAddTransaction(String type) {
        Long accountId = viewModel.getSelectedAccountIdValue();
        startActivity(AddTransactionActivity.newIntent(requireContext(), type,
                accountId != null ? accountId : -1L));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (balanceAnimator != null) {
            balanceAnimator.cancel();
            balanceAnimator = null;
        }
        binding = null;
    }
}
