package hotel.ui;

import hotel.model.*;
import hotel.model.Employee.Role;
import hotel.service.HotelService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class HotelApp extends JFrame {

    // ── palette ────────────────────────────────────────────────────────────
    static final Color C_NAVY       = new Color(0x1A237E);
    static final Color C_NAVY_LIGHT = new Color(0x283593);
    static final Color C_GOLD       = new Color(0xFFB300);
    static final Color C_BG         = new Color(0xF0F2F5);
    static final Color C_WHITE      = Color.WHITE;
    static final Color C_GREEN      = new Color(0x2E7D32);
    static final Color C_BLUE       = new Color(0x1565C0);
    static final Color C_PURPLE     = new Color(0x6A1B9A);
    static final Color C_RED        = new Color(0xC62828);
    static final Color C_ORANGE     = new Color(0xE65100);
    static final Color C_TEAL       = new Color(0x00695C);
    static final Color C_HEADER_TXT = new Color(0x37474F);
    static final Color C_ROW_ALT    = new Color(0xEEF2FF);

    static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    static final Font FONT_SUB    = new Font("Segoe UI", Font.PLAIN, 12);
    static final Font FONT_BOLD   = new Font("Segoe UI", Font.BOLD,  13);
    static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);

    private static final HotelService service = new HotelService();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private DefaultTableModel reservationModel;
    private DefaultTableModel roomModel;

    // ── constructor ─────────────────────────────────────────────────────────
    public HotelApp() {
        setTitle("Grand Metro Hotel - Reservation System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 740);
        setLocationRelativeTo(null);
        setBackground(C_BG);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(),   BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, C_NAVY, getWidth(), 0, new Color(0x4527A0));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(0, 80));
        header.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 24));

        // hotel name + tagline
        JPanel textBlock = new JPanel();
        textBlock.setLayout(new BoxLayout(textBlock, BoxLayout.Y_AXIS));
        textBlock.setOpaque(false);

        JLabel lblName = new JLabel("GRAND METRO HOTEL");
        lblName.setFont(FONT_TITLE);
        lblName.setForeground(C_GOLD);

        JLabel lblSub = new JLabel("Main Market, Metro City  |  100 Rooms  |  20 Staff  |  Est. 2010");
        lblSub.setFont(FONT_SUB);
        lblSub.setForeground(new Color(0xB0BEC5));

        textBlock.add(lblName);
        textBlock.add(Box.createVerticalStrut(2));
        textBlock.add(lblSub);

        // live date badge
        JLabel dateLbl = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        dateLbl.setFont(FONT_BOLD);
        dateLbl.setForeground(C_GOLD);
        dateLbl.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(C_GOLD, 1, 10), BorderFactory.createEmptyBorder(4, 12, 4, 12)));

        header.add(textBlock, BorderLayout.WEST);
        header.add(dateLbl,   BorderLayout.EAST);
        return header;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TABBED PANE (custom renderer)
    // ═══════════════════════════════════════════════════════════════════════
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setFont(FONT_BOLD);
        tabs.setBackground(C_BG);

        tabs.addTab("  Reservations  ", buildReservationsTab());
        tabs.addTab("  Rooms         ", buildRoomsTab());
        tabs.addTab("  Employees     ", buildEmployeesTab());
        tabs.addTab("  Reports       ", buildReportsTab());

        // color the tab strip
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override protected void installDefaults() {
                super.installDefaults();
                highlight        = C_BG;
                lightHighlight   = C_BG;
                shadow           = C_BG;
                darkShadow       = C_BG;
                focus            = C_BG;
            }
            @Override protected void paintTabBackground(Graphics g, int tp, int ti,
                    int x, int y, int w, int h, boolean sel) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color[] colors = {C_NAVY, C_BLUE, C_TEAL, new Color(0x4A148C)};
                g2.setColor(sel ? colors[ti % colors.length] : new Color(0xDDE1EA));
                g2.fillRoundRect(x + 2, y + 2, w - 4, h + 4, 10, 10);
                g2.dispose();
            }
            @Override protected void paintTabBorder(Graphics g, int tp, int ti,
                    int x, int y, int w, int h, boolean sel) {}
            @Override protected void paintFocusIndicator(Graphics g, int tp,
                    Rectangle[] rs, int ti, Rectangle ir, Rectangle tr, boolean sel) {}
        });

        UIManager.put("TabbedPane.selected",    C_NAVY);
        UIManager.put("TabbedPane.foreground",  C_WHITE);
        tabs.setForeground(C_WHITE);

        return tabs;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 1  RESERVATIONS
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildReservationsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ── button bar ──────────────────────────────────────────────────────
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnBar.setOpaque(false);

        FlatButton btnNew     = new FlatButton("+ New Booking",  C_GREEN);
        FlatButton btnCI      = new FlatButton("Check-In",       C_BLUE);
        FlatButton btnCO      = new FlatButton("Check-Out",      C_PURPLE);
        FlatButton btnCancel  = new FlatButton("Cancel Booking", C_RED);
        FlatButton btnRefresh = new FlatButton("Refresh",        C_TEAL);

        for (FlatButton b : new FlatButton[]{btnNew, btnCI, btnCO, btnCancel, btnRefresh})
            btnBar.add(b);

        // ── table ───────────────────────────────────────────────────────────
        String[] cols = {"Booking #", "Guest", "Room", "Type",
                         "Check-In", "Check-Out", "Nights", "Amount (Rs)", "Status"};
        reservationModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(reservationModel);
        table.getColumnModel().getColumn(8).setCellRenderer(new StatusBadgeRenderer());
        refreshReservationTable();

        btnNew.addActionListener(e -> openNewBookingDialog());
        btnCI.addActionListener(e  -> performAction("CHECK_IN",  table));
        btnCO.addActionListener(e  -> performAction("CHECK_OUT", table));
        btnCancel.addActionListener(e -> performAction("CANCEL", table));
        btnRefresh.addActionListener(e -> refreshReservationTable());

        JLabel hint = new JLabel("  Select a row, then use the action buttons above.");
        hint.setFont(FONT_SMALL);
        hint.setForeground(new Color(0x90A4AE));

        panel.add(btnBar, BorderLayout.NORTH);
        panel.add(styledScroll(table), BorderLayout.CENTER);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshReservationTable() {
        reservationModel.setRowCount(0);
        for (Reservation r : service.getAllReservations()) {
            reservationModel.addRow(new Object[]{
                r.getReservationId(), r.getCustomer().getName(),
                r.getRoom().getRoomNumber(), r.getRoom().getType(),
                r.getCheckIn().format(FMT), r.getCheckOut().format(FMT),
                r.getNights(), String.format("%.0f", r.getTotalAmount()),
                r.getStatus().name()
            });
        }
    }

    // ── new booking dialog ──────────────────────────────────────────────────
    private void openNewBookingDialog() {
        JDialog dlg = new JDialog(this, "New Reservation", true);
        dlg.setSize(500, 500);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // gradient title panel
        JPanel titleBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,C_NAVY,getWidth(),0,C_BLUE));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        titleBar.setPreferredSize(new Dimension(0, 52));
        JLabel tlbl = new JLabel("  New Room Reservation");
        tlbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tlbl.setForeground(C_WHITE);
        titleBar.add(tlbl, BorderLayout.CENTER);

        // form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(C_WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(18, 24, 12, 24));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(7, 6, 7, 6);
        gc.fill   = GridBagConstraints.HORIZONTAL;

        JTextField txtName  = fancyField("e.g. Rahul Sharma");
        JTextField txtPhone = fancyField("10-digit mobile");
        JTextField txtId    = fancyField("Aadhaar / Passport / DL");
        JTextField txtRoom  = fancyField("e.g. 201");
        JTextField txtCI    = fancyField("dd-MM-yyyy");
        JTextField txtCO    = fancyField("dd-MM-yyyy");

        String[] labels = {"Guest Name", "Phone", "ID Proof", "Room Number",
                           "Check-In", "Check-Out"};
        JTextField[] fields = {txtName, txtPhone, txtId, txtRoom, txtCI, txtCO};

        for (int i = 0; i < labels.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(FONT_BOLD);
            lbl.setForeground(C_HEADER_TXT);
            form.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.65;
            form.add(fields[i], gc);
        }

        // availability hint card
        JPanel hintCard = new JPanel(new GridLayout(0, 1, 0, 2));
        hintCard.setBackground(new Color(0xE8EAF6));
        hintCard.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(0x9FA8DA), 1, 8),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        Color[] typeColors = {C_TEAL, C_BLUE, C_ORANGE, C_PURPLE};
        int ci = 0;
        for (RoomType t : RoomType.values()) {
            long cnt = service.getAvailableRoomsByType(t).size();
            JLabel l = new JLabel(String.format("%-7s  %2d rooms available  @  Rs %.0f / night",
                    t, cnt, t.getPricePerNight()));
            l.setFont(FONT_SMALL);
            l.setForeground(typeColors[ci++ % typeColors.length]);
            hintCard.add(l);
        }
        gc.gridx = 0; gc.gridy = labels.length; gc.gridwidth = 2; gc.weightx = 1;
        form.add(hintCard, gc);

        // confirm button
        FlatButton btnOk = new FlatButton("Confirm Booking", C_GREEN);
        btnOk.setPreferredSize(new Dimension(200, 40));
        gc.gridy = labels.length + 1;
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnWrap.setOpaque(false);
        btnWrap.add(btnOk);
        form.add(btnWrap, gc);

        btnOk.addActionListener(e -> {
            try {
                String name  = txtName.getText().trim();
                String phone = txtPhone.getText().trim();
                String idp   = txtId.getText().trim();
                int roomNo   = Integer.parseInt(txtRoom.getText().trim());
                LocalDate ci2 = LocalDate.parse(txtCI.getText().trim(), FMT);
                LocalDate co2 = LocalDate.parse(txtCO.getText().trim(), FMT);
                if (name.isEmpty() || phone.isEmpty() || idp.isEmpty())
                    throw new IllegalArgumentException("All fields are required.");
                Customer c = new Customer(name, phone, idp);
                Reservation res = service.makeReservation(c, roomNo, ci2, co2);
                showSuccess(dlg, "Booking #" + res.getReservationId() + " Confirmed!\n\n"
                    + "Guest   : " + name + "\n"
                    + "Room    : " + roomNo + " (" + res.getRoom().getType() + ")\n"
                    + "Dates   : " + ci2.format(FMT) + "  to  " + co2.format(FMT) + "\n"
                    + "Amount  : Rs " + String.format("%.0f", res.getTotalAmount()));
                refreshReservationTable();
                refreshRoomTable();
                dlg.dispose();
            } catch (DateTimeParseException ex) {
                showError(dlg, "Date format must be dd-MM-yyyy  (e.g. 15-03-2026)");
            } catch (Exception ex) {
                showError(dlg, ex.getMessage());
            }
        });

        dlg.setLayout(new BorderLayout());
        dlg.add(titleBar, BorderLayout.NORTH);
        dlg.add(form,     BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    private void performAction(String action, JTable table) {
        int row = table.getSelectedRow();
        if (row < 0) { showWarning(this, "Please select a booking row first."); return; }
        int bookingId = (int) reservationModel.getValueAt(row, 0);
        try {
            switch (action) {
                case "CHECK_IN" -> {
                    service.checkIn(bookingId);
                    showSuccess(this, "Guest checked in for Booking #" + bookingId);
                }
                case "CHECK_OUT" -> {
                    Reservation r = service.getAllReservations().stream()
                        .filter(res -> res.getReservationId() == bookingId).findFirst().orElseThrow();
                    service.checkOut(bookingId);
                    showSuccess(this, "Guest checked out successfully.\n\nBill: Rs "
                        + String.format("%.0f", r.getTotalAmount())
                        + "\nThank you for staying with us!");
                }
                case "CANCEL" -> {
                    int ok = JOptionPane.showConfirmDialog(this,
                        "Cancel Booking #" + bookingId + "?", "Confirm Cancellation",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (ok == JOptionPane.YES_OPTION) {
                        service.cancelReservation(bookingId);
                        showSuccess(this, "Booking #" + bookingId + " has been cancelled.");
                    }
                }
            }
            refreshReservationTable();
            refreshRoomTable();
        } catch (Exception ex) { showError(this, ex.getMessage()); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 2  ROOMS
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildRoomsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        String[] cols = {"Room #", "Type", "Description", "Price / Night (Rs)", "Status"};
        roomModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(roomModel);
        table.setDefaultRenderer(Object.class, new RoomRowRenderer());
        refreshRoomTable();

        // filter bar card
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(C_WHITE);
        filterCard.setBorder(new RoundBorder(new Color(0xCFD8DC), 1, 10));

        JLabel flbl = new JLabel("Filter:");
        flbl.setFont(FONT_BOLD);
        flbl.setForeground(C_HEADER_TXT);

        JComboBox<String> filter = styledCombo(
            "All Rooms", "Available Only", "SINGLE", "DOUBLE", "DELUXE", "SUITE");
        filter.addActionListener(e -> applyRoomFilter(filter.getSelectedItem().toString()));

        // color legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        legend.setOpaque(false);
        legend.add(legendDot(new Color(0x81C784), "Available"));
        legend.add(legendDot(new Color(0xE57373), "Occupied"));

        filterCard.add(flbl);
        filterCard.add(filter);
        filterCard.add(Box.createHorizontalStrut(20));
        filterCard.add(legend);

        panel.add(filterCard,           BorderLayout.NORTH);
        panel.add(styledScroll(table),  BorderLayout.CENTER);
        return panel;
    }

    private void refreshRoomTable() {
        roomModel.setRowCount(0);
        for (Room r : service.getAllRooms()) {
            roomModel.addRow(new Object[]{
                r.getRoomNumber(), r.getType().name(),
                r.getType().getDescription(),
                String.format("%.0f", r.getType().getPricePerNight()),
                r.isAvailable() ? "AVAILABLE" : "OCCUPIED"
            });
        }
    }

    private void applyRoomFilter(String f) {
        roomModel.setRowCount(0);
        List<Room> src = switch (f) {
            case "Available Only" -> service.getAvailableRooms();
            case "SINGLE"  -> service.getAvailableRoomsByType(RoomType.SINGLE);
            case "DOUBLE"  -> service.getAvailableRoomsByType(RoomType.DOUBLE);
            case "DELUXE"  -> service.getAvailableRoomsByType(RoomType.DELUXE);
            case "SUITE"   -> service.getAvailableRoomsByType(RoomType.SUITE);
            default        -> service.getAllRooms();
        };
        for (Room r : src) {
            roomModel.addRow(new Object[]{
                r.getRoomNumber(), r.getType().name(),
                r.getType().getDescription(),
                String.format("%.0f", r.getType().getPricePerNight()),
                r.isAvailable() ? "AVAILABLE" : "OCCUPIED"
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 3  EMPLOYEES
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildEmployeesTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        String[] cols = {"Emp #", "Name", "Role", "Salary (Rs / month)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(model);
        table.getColumnModel().getColumn(2).setCellRenderer(new RoleBadgeRenderer());

        JComboBox<String> roleFilter = styledCombo(
            "All", "MANAGER", "RECEPTIONIST", "HOUSEKEEPING", "SECURITY", "CHEF");

        Runnable populate = () -> {
            model.setRowCount(0);
            String sel = roleFilter.getSelectedItem().toString();
            List<Employee> list = sel.equals("All")
                ? service.getAllEmployees()
                : service.getEmployeesByRole(Role.valueOf(sel));
            for (Employee e : list)
                model.addRow(new Object[]{
                    e.getEmployeeId(), e.getName(),
                    e.getRole().name(), String.format("%.0f", e.getSalary())
                });
        };
        populate.run();
        roleFilter.addActionListener(e -> populate.run());

        // filter card
        JPanel filterCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        filterCard.setBackground(C_WHITE);
        filterCard.setBorder(new RoundBorder(new Color(0xCFD8DC), 1, 10));
        JLabel flbl = new JLabel("Filter by Role:");
        flbl.setFont(FONT_BOLD); flbl.setForeground(C_HEADER_TXT);
        filterCard.add(flbl);
        filterCard.add(roleFilter);

        // stat strip
        JPanel statStrip = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        statStrip.setOpaque(false);
        Color[] rc = {C_NAVY, C_TEAL, C_GREEN, C_ORANGE, C_PURPLE};
        String[] roles2 = {"MANAGER","RECEPTIONIST","HOUSEKEEPING","SECURITY","CHEF"};
        for (int i = 0; i < roles2.length; i++) {
            long cnt = service.getEmployeesByRole(Role.valueOf(roles2[i])).size();
            statStrip.add(miniCard(roles2[i], String.valueOf(cnt), rc[i]));
        }

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(filterCard, BorderLayout.NORTH);
        top.add(statStrip,  BorderLayout.SOUTH);

        panel.add(top,                  BorderLayout.NORTH);
        panel.add(styledScroll(table),  BorderLayout.CENTER);
        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  TAB 4  REPORTS (dashboard cards)
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildReportsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setBackground(C_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // ── top stat cards row ──────────────────────────────────────────────
        JPanel cardRow = new JPanel(new GridLayout(1, 4, 12, 0));
        cardRow.setOpaque(false);

        JLabel lTotalRooms  = statCard(cardRow, "Total Rooms",  "100",  C_NAVY,   "");
        JLabel lOccupied    = statCard(cardRow, "Occupied",     "0",    C_RED,    "");
        JLabel lAvailable   = statCard(cardRow, "Available",    "100",  C_GREEN,  "");
        JLabel lRevenue     = statCard(cardRow, "Revenue",      "Rs 0", C_ORANGE, "earned");

        // ── occupancy breakdown ─────────────────────────────────────────────
        JPanel breakCard = new JPanel(new GridLayout(0, 1, 0, 4));
        breakCard.setBackground(C_WHITE);
        breakCard.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(0xCFD8DC), 1, 12),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel breakTitle = new JLabel("Room-type breakdown");
        breakTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        breakTitle.setForeground(C_HEADER_TXT);
        breakCard.add(breakTitle);

        Color[] typeCol = {C_TEAL, C_BLUE, C_ORANGE, C_PURPLE};
        JLabel[] breakLabels = new JLabel[RoomType.values().length];
        int ti = 0;
        for (RoomType t : RoomType.values()) {
            breakLabels[ti] = new JLabel();
            breakLabels[ti].setFont(FONT_NORMAL);
            breakLabels[ti].setForeground(typeCol[ti]);
            breakCard.add(breakLabels[ti]);
            ti++;
        }

        // ── bookings summary ────────────────────────────────────────────────
        JPanel sumCard = new JPanel(new GridLayout(0, 1, 0, 4));
        sumCard.setBackground(C_WHITE);
        sumCard.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(0xCFD8DC), 1, 12),
            BorderFactory.createEmptyBorder(14, 18, 14, 18)));

        JLabel sumTitle = new JLabel("Reservation summary");
        sumTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sumTitle.setForeground(C_HEADER_TXT);
        sumCard.add(sumTitle);

        JLabel lConfirmed  = new JLabel(); lConfirmed.setForeground(C_BLUE);
        JLabel lCheckedIn  = new JLabel(); lCheckedIn.setForeground(C_GREEN);
        JLabel lCheckedOut = new JLabel(); lCheckedOut.setForeground(C_HEADER_TXT);
        JLabel lCancelled  = new JLabel(); lCancelled.setForeground(C_RED);
        for (JLabel l : new JLabel[]{lConfirmed,lCheckedIn,lCheckedOut,lCancelled}) {
            l.setFont(FONT_NORMAL);
            sumCard.add(l);
        }

        JPanel bottomRow = new JPanel(new GridLayout(1, 2, 12, 0));
        bottomRow.setOpaque(false);
        bottomRow.add(breakCard);
        bottomRow.add(sumCard);

        // ── refresh button ──────────────────────────────────────────────────
        FlatButton btnR = new FlatButton("Refresh Reports", C_TEAL);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnWrap.setOpaque(false);
        btnWrap.add(btnR);

        Runnable refresh = () -> {
            long total    = service.getAllRooms().size();
            long occupied = service.getAllRooms().stream().filter(r -> !r.isAvailable()).count();
            double rev    = service.totalRevenue();

            lTotalRooms.setText(String.valueOf(total));
            lOccupied.setText(occupied + "  (" + String.format("%.0f%%", occupied*100.0/total) + ")");
            lAvailable.setText(String.valueOf(total - occupied));
            lRevenue.setText("Rs " + String.format("%.0f", rev));

            int bi = 0;
            for (RoomType t : RoomType.values()) {
                long tot  = service.getAllRooms().stream().filter(r -> r.getType()==t).count();
                long free = service.getAvailableRoomsByType(t).size();
                breakLabels[bi++].setText(String.format(
                    "  %-7s   %2d total    %2d available    %2d occupied",
                    t, tot, free, tot-free));
            }

            long conf  = countStatus(Reservation.Status.CONFIRMED);
            long cin   = countStatus(Reservation.Status.CHECKED_IN);
            long cout  = countStatus(Reservation.Status.CHECKED_OUT);
            long canc  = countStatus(Reservation.Status.CANCELLED);
            lConfirmed.setText("  Confirmed    : " + conf);
            lCheckedIn.setText("  Checked-In   : " + cin);
            lCheckedOut.setText("  Checked-Out  : " + cout);
            lCancelled.setText("  Cancelled    : " + canc);
        };

        refresh.run();
        btnR.addActionListener(e -> refresh.run());

        panel.add(btnWrap,   BorderLayout.NORTH);
        panel.add(cardRow,   BorderLayout.CENTER);
        panel.add(bottomRow, BorderLayout.SOUTH);
        return panel;
    }

    private long countStatus(Reservation.Status s) {
        return service.getAllReservations().stream()
               .filter(r -> r.getStatus() == s).count();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  FOOTER
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(new Color(0x263238));
        footer.setPreferredSize(new Dimension(0, 28));
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

        JLabel left = new JLabel("Grand Metro Hotel  |  Reservation Management System  v1.0");
        left.setFont(FONT_SMALL);
        left.setForeground(new Color(0x90A4AE));

        JLabel right = new JLabel("10 avg customers/day  |  100 rooms  |  20 staff");
        right.setFont(FONT_SMALL);
        right.setForeground(new Color(0x90A4AE));

        footer.add(left,  BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  COMPONENT FACTORIES
    // ═══════════════════════════════════════════════════════════════════════
    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(FONT_NORMAL);
        t.setRowHeight(30);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setSelectionBackground(new Color(0xC5CAE9));
        t.setSelectionForeground(C_NAVY);
        t.setBackground(C_WHITE);

        JTableHeader h = t.getTableHeader();
        h.setFont(FONT_BOLD);
        h.setBackground(C_NAVY);
        h.setForeground(C_WHITE);
        h.setPreferredSize(new Dimension(0, 36));
        h.setReorderingAllowed(false);

        t.setDefaultRenderer(Object.class, new StripedRenderer());
        return t;
    }

    private JScrollPane styledScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new RoundBorder(new Color(0xCFD8DC), 1, 10));
        sp.getViewport().setBackground(C_WHITE);
        return sp;
    }

    private JTextField fancyField(String placeholder) {
        JTextField f = new JTextField(22);
        f.setFont(FONT_NORMAL);
        f.setForeground(new Color(0x37474F));
        f.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(new Color(0xB0BEC5), 1, 6),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setToolTipText(placeholder);
        return f;
    }

    private JComboBox<String> styledCombo(String... items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_NORMAL);
        cb.setBackground(C_WHITE);
        cb.setPreferredSize(new Dimension(160, 30));
        return cb;
    }

    /** Large stat card with big value label — returns the value JLabel for live updates. */
    private JLabel statCard(JPanel parent, String title, String value, Color accent, String sub) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, accent, 0, getHeight(),
                        accent.darker().darker()));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),14,14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lTitle.setForeground(new Color(255,255,255,200));

        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lValue.setForeground(C_WHITE);

        JLabel lSub = new JLabel(sub.isEmpty() ? " " : sub);
        lSub.setFont(FONT_SMALL);
        lSub.setForeground(new Color(255,255,255,160));

        card.add(lTitle, BorderLayout.NORTH);
        card.add(lValue, BorderLayout.CENTER);
        card.add(lSub,   BorderLayout.SOUTH);

        parent.add(card);
        return lValue;
    }

    private JPanel miniCard(String label, String value, Color color) {
        JPanel p = new JPanel(new BorderLayout(4, 0));
        p.setBackground(C_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(color, 2, 8),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
        JLabel lv = new JLabel(value);
        lv.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lv.setForeground(color);
        JLabel ll = new JLabel(label);
        ll.setFont(FONT_SMALL);
        ll.setForeground(C_HEADER_TXT);
        p.add(lv, BorderLayout.WEST);
        p.add(ll, BorderLayout.CENTER);
        return p;
    }

    private JPanel legendDot(Color color, String text) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel dot = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, 14, 14);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(14, 14); }
        };
        dot.setOpaque(false);
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SMALL);
        lbl.setForeground(C_HEADER_TXT);
        p.add(dot); p.add(lbl);
        return p;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DIALOG HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private void showSuccess(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success",  JOptionPane.INFORMATION_MESSAGE);
    }
    private void showError(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error",    JOptionPane.ERROR_MESSAGE);
    }
    private void showWarning(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Warning",  JOptionPane.WARNING_MESSAGE);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  INNER CLASSES
    // ═══════════════════════════════════════════════════════════════════════

    /** Alternating-stripe table renderer. */
    static class StripedRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(FONT_NORMAL);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!sel) setBackground(row % 2 == 0 ? C_WHITE : C_ROW_ALT);
            return this;
        }
    }

    /** Colored badge for reservation status column. */
    static class StatusBadgeRenderer extends DefaultTableCellRenderer {
        private static final java.util.Map<String, Color> COLORS = java.util.Map.of(
            "CONFIRMED",   C_BLUE,
            "CHECKED_IN",  C_GREEN,
            "CHECKED_OUT", new Color(0x546E7A),
            "CANCELLED",   C_RED
        );
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            String status = v == null ? "" : v.toString();
            JPanel badge = new JPanel(new GridBagLayout());
            badge.setBackground(sel ? t.getSelectionBackground() :
                    (row % 2 == 0 ? C_WHITE : C_ROW_ALT));
            Color c = COLORS.getOrDefault(status, Color.GRAY);
            JLabel lbl = new JLabel(status.replace("_", " "));
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(C_WHITE);
            lbl.setOpaque(true);
            lbl.setBackground(c);
            lbl.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
            badge.add(lbl);
            return badge;
        }
    }

    /** Colored badge for employee role column. */
    static class RoleBadgeRenderer extends DefaultTableCellRenderer {
        private static final java.util.Map<String, Color> COLORS = java.util.Map.of(
            "MANAGER",       C_NAVY,
            "RECEPTIONIST",  C_TEAL,
            "HOUSEKEEPING",  C_GREEN,
            "SECURITY",      C_ORANGE,
            "CHEF",          C_PURPLE
        );
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            String role = v == null ? "" : v.toString();
            JPanel badge = new JPanel(new GridBagLayout());
            badge.setBackground(sel ? t.getSelectionBackground() :
                    (row % 2 == 0 ? C_WHITE : C_ROW_ALT));
            Color c = COLORS.getOrDefault(role, Color.GRAY);
            JLabel lbl = new JLabel(role);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setForeground(c);
            lbl.setBorder(BorderFactory.createCompoundBorder(
                new RoundBorder(c, 1, 6),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
            badge.add(lbl);
            return badge;
        }
    }

    /** Green/red row tinting for the Rooms table. */
    static class RoomRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, focus, row, col);
            setFont(FONT_NORMAL);
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!sel) {
                String status = (String) t.getModel().getValueAt(row, 4);
                setBackground("OCCUPIED".equals(status)
                    ? new Color(0xFFEBEE) : new Color(0xE8F5E9));
                setForeground("OCCUPIED".equals(status)
                    ? new Color(0xB71C1C) : new Color(0x1B5E20));
            }
            return this;
        }
    }

    /** Custom gradient "pill" button with hover animation. */
    static class FlatButton extends JButton {
        private final Color base;
        private boolean hovered = false;

        FlatButton(String text, Color color) {
            super(text);
            this.base = color;
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setPreferredSize(new Dimension(getPreferredSize().width + 20, 36));

            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color top = hovered ? base.brighter() : base;
            Color bot = hovered ? base : base.darker();
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bot));
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 18, 18));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Thin rounded border. */
    static class RoundBorder extends AbstractBorder {
        private final Color color;
        private final int thickness, radius;

        RoundBorder(Color color, int thickness, int radius) {
            this.color = color; this.thickness = thickness; this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.draw(new RoundRectangle2D.Float(x + 1, y + 1, w - 2, h - 2, radius, radius));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) { return new Insets(radius/2,radius/2,radius/2,radius/2); }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  MAIN
    // ═══════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(HotelApp::new);
    }
}
