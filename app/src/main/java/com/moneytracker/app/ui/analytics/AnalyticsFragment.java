package com.moneytracker.app.ui.analytics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.moneytracker.app.R;
import com.moneytracker.app.data.dao.TransactionDao;
import com.moneytracker.app.databinding.FragmentAnalyticsBinding;
import com.moneytracker.app.ui.widget.SegmentedToggle;
import com.moneytracker.app.util.CurrencyFormatter;
import com.moneytracker.app.util.DisplayNameResolver;
import com.moneytracker.app.util.ViewAnimations;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsFragment extends Fragment {

    private FragmentAnalyticsBinding binding;
    private AnalyticsViewModel viewModel;
    private SegmentedToggle periodToggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        binding = FragmentAnalyticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AnalyticsViewModel.class);
        ViewAnimations.fadeInContent(view);

        setupChart();

        periodToggle = new SegmentedToggle(binding.periodContainer, binding.periodIndicator,
                new android.widget.TextView[]{binding.periodWeek, binding.periodMonth, binding.periodYear},
                requireContext().getColor(R.color.on_accent_text), requireContext().getColor(R.color.text_secondary));
        AnalyticsViewModel.Period[] periods = AnalyticsViewModel.Period.values();
        periodToggle.setOnSegmentSelectedListener(index -> viewModel.setPeriod(periods[index]));

        viewModel.getPeriod().observe(getViewLifecycleOwner(), period -> periodToggle.setSelectedIndex(period.ordinal()));
        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), value ->
                binding.textTotalIncome.setText(CurrencyFormatter.format(value != null ? value : 0, null)));
        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), value ->
                binding.textTotalExpense.setText(CurrencyFormatter.format(value != null ? value : 0, null)));
        viewModel.getCategoryTotals().observe(getViewLifecycleOwner(), this::renderChart);
    }

    private void setupChart() {
        binding.pieChart.getDescription().setEnabled(false);
        binding.pieChart.setUsePercentValues(true);
        binding.pieChart.setDrawHoleEnabled(true);
        binding.pieChart.setHoleRadius(58f);
        binding.pieChart.setTransparentCircleRadius(62f);
        binding.pieChart.setHoleColor(android.graphics.Color.WHITE);
        binding.pieChart.setEntryLabelColor(android.graphics.Color.WHITE);
        binding.pieChart.setEntryLabelTextSize(11f);
        Legend legend = binding.pieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setWordWrapEnabled(true);
    }

    private void renderChart(List<TransactionDao.CategoryTotal> totals) {
        boolean empty = totals == null || totals.isEmpty();
        binding.textNoData.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.pieChart.setVisibility(empty ? View.INVISIBLE : View.VISIBLE);
        if (empty) {
            return;
        }

        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        for (TransactionDao.CategoryTotal total : totals) {
            String label = DisplayNameResolver.resolve(requireContext(), total.categoryName);
            entries.add(new PieEntry((float) total.total, label));
            colors.add(total.color != 0 ? total.color : requireContext().getColor(R.color.text_tertiary));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setValueTextColor(android.graphics.Color.WHITE);
        dataSet.setValueTextSize(12f);
        dataSet.setSliceSpace(2f);

        PieData data = new PieData(dataSet);
        binding.pieChart.setData(data);
        binding.pieChart.animateY(900, Easing.EaseOutCirc);
        binding.pieChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
